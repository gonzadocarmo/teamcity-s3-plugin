package com.dcn.teamcity.awsS3Plugin;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.util.StringUtils;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.BuildProblemTypes;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.ArtifactsPreprocessor;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsBuilder;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3BuildProcessAdapterHelper {

    private BuildProgressLogger myLogger;

    public AWSS3BuildProcessAdapterHelper withLogger(BuildProgressLogger logger){
        this.myLogger = logger;
        return this;
    }

    public @NotNull BuildProblemData createBuildProblemData(String bucketName, String filePath, Exception e) {
        String bucketID = bucketName.length() <= 12 ? bucketName : bucketName.substring(0, 11);
        String filePathID = filePath.length() <= 30 ? filePath : filePath.substring(filePath.length() - 30);

        final String identity = String.format("%s:%s:%s",
                bucketID,
                filePathID,
                Calendar.getInstance().getTimeInMillis());
        return BuildProblemData.createBuildProblem(identity, BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, e.getMessage(), e.getStackTrace().toString());
    }

    public BuildProblemData createBuildProblemData(String bucketName, Exception e) {
        final String identity = String.format("%s:%s",
                bucketName,
                Calendar.getInstance().getTimeInMillis());
        return BuildProblemData.createBuildProblem(identity, BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, e.getMessage(), e.getStackTrace().toString());
    }

    public BuildProblemData createBuildProblemData(String bucketName, String message) {
        final String identity = String.format("%s:%s",
                bucketName,
                Calendar.getInstance().getTimeInMillis());
        return BuildProblemData.createBuildProblem(identity, BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, message);
    }

    public @NotNull AmazonS3 createClient(String AWSPublicKey, String AWSPrivateKey, String region) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        return createClientWithRegion(AWSPublicKey, AWSPrivateKey, region, clientConfiguration);
    }

    public @NotNull AmazonS3 createClientWithProxy(String AWSPublicKey, String AWSPrivateKey, String region, String proxyUrl) throws MalformedURLException {
        ClientConfiguration clientConfiguration = this.createClientConfiguration(proxyUrl);
        return createClientWithRegion(AWSPublicKey, AWSPrivateKey, region, clientConfiguration);
    }

    private AmazonS3 createClientWithRegion(String publicKey, String privateKey, String region, ClientConfiguration clientConfiguration){
        final Regions defaultRegion = Regions.DEFAULT_REGION;
        Regions clientRegion;

        try {
          clientRegion = Regions.fromName(region);
        } catch (IllegalArgumentException iae) {
          myLogger.warning(String.format("Region '%s' not valid. Using default region: '%s'...", region, defaultRegion.getName()));
          clientRegion = defaultRegion;
        }

        return AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(publicKey, privateKey)))
                .withClientConfiguration(clientConfiguration)
                .build();
    }

    public @NotNull ClientConfiguration createClientConfiguration(String proxyUrl) throws MalformedURLException {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        URL url = new URL(proxyUrl);

        String userInfo = url.getUserInfo();

        if (userInfo != null) {
            String username = url.getUserInfo();

            final int password_idx = userInfo.indexOf(':');
            if (password_idx != -1) {
                username = userInfo.substring(0, password_idx);
                final String password = userInfo.substring(userInfo.indexOf(':') + 1);
                clientConfiguration.setProxyPassword(password);
            }

            final int domain_idx = username.indexOf('\\');

            if (domain_idx != -1) {
                final String domain = username.substring(0, domain_idx);
                username = username.substring(domain_idx + 1);
                clientConfiguration.setProxyDomain(domain);
            }

            clientConfiguration.setProxyUsername(username);
        }

        clientConfiguration.setProtocol(Protocol.HTTPS.toString().equals(url.getProtocol()) ? Protocol.HTTPS : Protocol.HTTP);
        clientConfiguration.setProxyHost(url.getHost());
        clientConfiguration.setProxyPort(url.getPort());
        return clientConfiguration;
    }

    public @NotNull List<ArtifactsCollection> getArtifactsCollections(String sourcePaths, ExtensionHolder extensionHolder, File checkoutDirectory) {
        final Collection<ArtifactsPreprocessor> preprocessors = extensionHolder.getExtensions(ArtifactsPreprocessor.class);

        final ArtifactsBuilder builder = new ArtifactsBuilder();
        builder.setPreprocessors(preprocessors);
        builder.setBaseDir(checkoutDirectory);
        builder.setArtifactsPaths(sourcePaths);

        return builder.build();
    }

    public @NotNull AgentRunnerBuildParametersModel createModel(@NotNull final Map<String, String> runnerParameters) {
        final String bucketName = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_BUCKET_NAME));
        final String bucketRegion = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_BUCKET_REGION));
        final String credentialsPublicKey = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY));
        final String credentialsPrivateKey = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY));
        final String sourcePaths = runnerParameters.get(PluginConstants.UI_PARAM_CONTENT_PATHS);
        final boolean needToEmptyBucket = Boolean.valueOf(runnerParameters.get(PluginConstants.UI_PARAM_EMPTY_BUCKET));
        final String httpHeaderCacheControl = StringUtil.nullIfEmpty(StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL)));
        final String httpProxy = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_HTTP_PROXY));

        return new AgentRunnerBuildParametersModel(bucketName, bucketRegion, credentialsPublicKey, credentialsPrivateKey, needToEmptyBucket, sourcePaths, httpHeaderCacheControl, httpProxy);
    }
}
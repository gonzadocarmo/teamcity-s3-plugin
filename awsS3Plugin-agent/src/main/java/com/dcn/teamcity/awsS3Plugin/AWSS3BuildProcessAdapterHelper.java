package com.dcn.teamcity.awsS3Plugin;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.StringUtils;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.BuildProblemTypes;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.ArtifactsPreprocessor;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsBuilder;
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by sg0216948 on 7/12/16.
 *
 * @author <a href="mailto:gonzalo.docarmo@gmail.com">Gonzalo G. do Carmo Norte</a>
 */
public class AWSS3BuildProcessAdapterHelper {

    public @NotNull BuildProblemData createBuildProblemData(String bucketName, String filePath, String exceptionMessage) {
        String bucketID = bucketName.length() <= 12 ? bucketName : bucketName.substring(0, 11);
        String filePathID = filePath.length() <= 30 ? filePath : filePath.substring(filePath.length() - 30);

        final String id = String.format("%s:%s:%s",
                bucketID,
                filePathID,
                Calendar.getInstance().getTimeInMillis());
        return BuildProblemData.createBuildProblem(id, BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, exceptionMessage);
    }

    public BuildProblemData createBuildProblemData(String bucketName, String exceptionMessage) {
        final String id = String.format("%s:%s",
                bucketName,
                Calendar.getInstance().getTimeInMillis());
        return BuildProblemData.createBuildProblem(id, BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, exceptionMessage);
    }

    public @NotNull AmazonS3 createClient(String AWSPublickKey, String AWSPrivateKey) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        return new AmazonS3Client(new BasicAWSCredentials(AWSPublickKey, AWSPrivateKey), clientConfiguration);
    }

    public @NotNull AmazonS3 createClientWithProxy(String AWSPublickKey, String AWSPrivateKey, String proxyUrl) throws Exception {
        ClientConfiguration clientConfiguration = this.createClientConfiguration(proxyUrl);
        return new AmazonS3Client(new BasicAWSCredentials(AWSPublickKey, AWSPrivateKey), clientConfiguration);
    }

    public @NotNull ClientConfiguration createClientConfiguration(String proxyUrl) throws Exception {
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
        final String credentialsPublicKey = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_CREDENTIALS_PUB_KEY));
        final String credentialsPrivateKey = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_CREDENTIALS_PRIVATE_KEY));
        final String sourcePaths = runnerParameters.get(PluginConstants.UI_PARAM_CONTENT_PATHS);
        final boolean needToEmptyBucket = Boolean.valueOf(runnerParameters.get(PluginConstants.UI_PARAM_EMPTY_BUCKET));
        final String httpHeaderCacheControl = StringUtil.nullIfEmpty(StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_HTTP_HEADERS_CACHE_CONTROL)));
        final String httpProxy = StringUtils.trim(runnerParameters.get(PluginConstants.UI_PARAM_HTTP_PROXY));

        return new AgentRunnerBuildParametersModel(bucketName, credentialsPublicKey, credentialsPrivateKey, needToEmptyBucket, sourcePaths, httpHeaderCacheControl, httpProxy);
    }
}
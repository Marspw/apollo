package com.ctrip.apollo.configserver.controller;

import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.core.model.ApolloConfig;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
public class ConfigController {
    @Resource(name = "configService")
    private ConfigService configService;

    @RequestMapping(value = "/{appId}/{clusterName}/{versionName:.*}")
    public ApolloConfig queryConfig(@PathVariable long appId,
                                    @PathVariable String clusterName,
                                    @PathVariable String versionName,
                                    @RequestParam(value = "releaseId", defaultValue = "-1") long clientSideReleaseId,
                                    HttpServletResponse response) throws IOException {
        Version version = configService.loadVersionByAppIdAndVersionName(appId, versionName);
        if (version == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                String.format("Could not load version with appId: %d, versionName: %s", appId, versionName));
            return null;
        }
        if (version.getReleaseId() == clientSideReleaseId) {
            //Client side configuration is the same with server side, return 304
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
        }

        ApolloConfig apolloConfig =
            configService.loadConfigByVersionAndClusterName(version, clusterName);

        if (apolloConfig == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                String.format("Could not load config with releaseId: %d, clusterName: %s", version.getReleaseId(), clusterName));
            return null;
        }

        return apolloConfig;
    }
}

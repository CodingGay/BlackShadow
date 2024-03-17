/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package top.niunaijun.shadow.loader;

import android.content.ComponentName;
import android.content.Context;

import com.tencent.shadow.core.loader.infos.ContainerProviderInfo;
import com.tencent.shadow.core.loader.managers.ComponentManager;

import org.jetbrains.annotations.NotNull;

import top.niunaijun.shadow.common.PluginConfig;
import top.niunaijun.shadow.manager.BSManifestManager;

public class BSComponentManager extends ComponentManager {

    private final Context mContext;
    private final PluginConfig mPluginConfig;

    public BSComponentManager(Context context, PluginConfig config) {
        this.mContext = context;
        this.mPluginConfig = config;
    }

    /**
     * 配置插件Activity 到 壳子Activity的对应关系
     *
     * @param pluginActivity 插件Activity
     * @return 壳子Activity
     */
    @NotNull
    @Override
    public ComponentName onBindContainerActivity(ComponentName pluginActivity) {
        switch (pluginActivity.getClassName()) {
            /**
             * 这里配置对应的对应关系
             */
        }
        return new ComponentName(mContext, BSManifestManager.getPluginProxyActivity(mPluginConfig.getBPid()));
    }

    /**
     * 配置对应宿主中预注册的壳子contentProvider的信息
     */
    @NotNull
    @Override
    public ContainerProviderInfo onBindContainerContentProvider(ComponentName pluginContentProvider) {
        return new ContainerProviderInfo(
                BSManifestManager.getPluginContainerContentProviderClassName(mPluginConfig.getBPid()),
                BSManifestManager.getPluginContainerContentProviderAuth(mPluginConfig.getBPid()));
    }

}

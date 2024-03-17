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

import android.content.Context;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.loader.ShadowPluginLoader;
import com.tencent.shadow.core.loader.exceptions.LoadPluginException;
import com.tencent.shadow.core.loader.managers.ComponentManager;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

import top.niunaijun.shadow.common.PluginConfig;

public class BSPluginLoader extends ShadowPluginLoader {

    private final ComponentManager mComponentManager;
    private final PluginConfig mPluginConfig;

    public BSPluginLoader(Context hostAppContext, PluginConfig pluginConfig) {
        super(hostAppContext);
        this.mComponentManager = new BSComponentManager(hostAppContext, pluginConfig);
        this.mPluginConfig = pluginConfig;
    }

    @NotNull
    @Override
    public ComponentManager getComponentManager() {
        return mComponentManager;
    }

    @NotNull
    @Override
    public String getDelegateProviderKey() {
        return this.mPluginConfig.getPluginKey();
    }

    @NotNull
    @Override
    public Future<?> loadPlugin(@NotNull InstalledApk installedApk) throws LoadPluginException {
        return super.loadPlugin(installedApk);
    }
}

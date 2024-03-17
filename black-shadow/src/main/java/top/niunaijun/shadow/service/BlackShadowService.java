package top.niunaijun.shadow.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BlackShadowService {
    private static final List<IBlackShadowService> sShadowServices = new ArrayList<>();

    static {
        sShadowServices.add(BSManagerService.get());
        sShadowServices.add(BSProcessService.get());
    }

    public static boolean startup() {
        for (IBlackShadowService service : sShadowServices) {
            service.startup();
        }
        return true;
    }
}

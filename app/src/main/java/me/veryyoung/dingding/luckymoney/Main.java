package me.veryyoung.dingding.luckymoney;

import android.view.MotionEvent;
import android.view.View;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorBestMatch;
import static de.robv.android.xposed.XposedHelpers.getObjectField;


public class Main implements IXposedHookLoadPackage {

    private static final String DINGDING_PACKAGE_NAME = "com.alibaba.android.rimet";

    private static final String MAP_CLASS_NAME = "com.alibaba.android.rimet.biz.im.notification.MessageNotificationManager";
    private static final String MAP_FUNCTION_NAME = "a";


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            final Class<?> message = findClass("com.alibaba.wukong.im.Message", lpparam.classLoader);

            findAndHookMethod(MAP_CLASS_NAME, lpparam.classLoader, MAP_FUNCTION_NAME, int.class, message, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            if (null != param.args[1]) {
                                Field messageContentFileld = param.args[1].getClass().getSuperclass().getSuperclass().getDeclaredField("mMessageContent");
                                String messageContent = messageContentFileld.get(param.args[1]).toString();
                                if (messageContent.startsWith("{\"tp\":902")) {
                                    JSONObject jsonObject = new JSONObject(messageContent);
                                    String ext = jsonObject.getJSONArray("multi").getJSONObject(0).getString("ext");
                                    ext = ext.replace("\\", "").replace("\"{", "{").replace("}\"", "}");
                                    jsonObject = new JSONObject(ext);
                                    Long sender = jsonObject.getLong("sid");
                                    String clusterId = jsonObject.getString("clusterid");

                                    Object RedPacketsRpc = callStaticMethod(findClass("wl", lpparam.classLoader), "a");
                                    Constructor constructor = findConstructorBestMatch(findClass("wl$9", lpparam.classLoader), RedPacketsRpc.getClass(), findClass("aae", lpparam.classLoader));

                                    callMethod(callStaticMethod(findClass("cdk", lpparam.classLoader), "a", findClass("com.alibaba.android.dingtalk.redpackets.idl.service.RedEnvelopPickIService", lpparam.classLoader)), "pickRedEnvelopCluster", sender, clusterId, constructor.newInstance(RedPacketsRpc, null));
                                }
                            }
                        }
                    }
            );


            findAndHookMethod("com.alibaba.android.dingtalk.redpackets.activities.PickRedPacketsActivity", lpparam.classLoader, MAP_FUNCTION_NAME, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View mView = (View) getObjectField(param.thisObject, "h");
                    mView.callOnClick();
                    mView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                }
            });


        }
    }


}
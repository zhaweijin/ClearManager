package com.hiveview.clear.manager;

import java.util.ArrayList;  
import java.util.Collections;  
import java.util.List;  
  

import android.content.Context;  
import android.content.pm.ApplicationInfo;  
import android.content.pm.PackageManager;  
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;  
  
  
public class RunningAppInfo {  
      
    private Context m_context;  
    private final String LogTag = "RunningAppInfoParam";  
    private static RunningAppInfo runningAppInfoParam;
    
    public static RunningAppInfo getAppInfoParam(Context context){
    	if(runningAppInfoParam==null){
    		runningAppInfoParam = new RunningAppInfo(context);
    	}
    	return runningAppInfoParam;
    }
    
    public RunningAppInfo(Context context) {  
        m_context = context;  
    }  
      
    public List<ApplicationInfo> getInstallAppInfo() {  
        PackageManager mypm = m_context.getPackageManager();  
        List<ApplicationInfo> appInfoList = mypm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);  
        Collections.sort(appInfoList, new ApplicationInfo.DisplayNameComparator(mypm));// 排序    
          
//        for(ApplicationInfo app: appInfoList) {  
//            //Log.v(LogTag, "RunningAppInfoParam  getInstallAppInfo app label = " + (String)app.loadLabel(umpm));  
//            //Log.v(LogTag, "RunningAppInfoParam  getInstallAppInfo app packageName = " + app.packageName);  
//        }  
          
        return appInfoList;  
    }  
      
    //获取第三方应用信息  
    public ArrayList<String> getThirdAppInfo() {  
        List<ApplicationInfo> appList = getInstallAppInfo();  
        List<ApplicationInfo> thirdAppList = new ArrayList<ApplicationInfo>();  
        thirdAppList.clear();  
        for (ApplicationInfo app : appList) {    
            //非系统程序    
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {    
                thirdAppList.add(app);  
            }     
            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了    
            else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){    
                thirdAppList.add(app);  
            }    
        }    
        PackageManager mypm = m_context.getPackageManager();  
        ArrayList<String> thirdAppNameList = new ArrayList<String>();  
        for(ApplicationInfo app : thirdAppList) {  
            Utils.print(LogTag, "RunningAppInfoParam getThirdAppInfo app label = " + (String)app.loadLabel(mypm)+",packagename="+app.packageName);  
//            thirdAppNameList.add((String)app.loadLabel(mypm)); 
            thirdAppNameList.add(app.packageName);
        }  
          
        return thirdAppNameList;  
    }  
      
    //获取系统应用信息  
    public ArrayList<String> getSystemAppInfo() {  
        List<ApplicationInfo> appList = getInstallAppInfo();  
        List<ApplicationInfo> sysAppList = new ArrayList<ApplicationInfo>();  
        sysAppList.clear();  
        for (ApplicationInfo app : appList) {    
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {    
                sysAppList.add(app);  
            }    
        }  
        PackageManager mypm = m_context.getPackageManager();  
        ArrayList<String> sysAppNameList = new ArrayList<String>();  
        for(ApplicationInfo app : sysAppList) {  
        	Utils.print(LogTag, "RunningAppInfoParam getSystemAppInfo app label = " + (String)app.loadLabel(mypm)+",packagename="+app.packageName);  
//            sysAppNameList.add((String)app.loadLabel(mypm));  
            sysAppNameList.add(app.packageName);
        }  
          
        return sysAppNameList;  
    }  
      
    
    
	/**
	 * 获取程序的名字
	 * 
	 * @param context
	 * @param packname
	 * @return
	 */
	public String getAppName(Context context, String packname) {
		// 包管理操作管理类
		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo info = pm.getApplicationInfo(packname, 0);
			return info.loadLabel(pm).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packname;
	}
	
	
	
	 
}  
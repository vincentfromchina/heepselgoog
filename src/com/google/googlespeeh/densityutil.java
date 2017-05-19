package com.google.googlespeeh;

import android.content.Context;

public class densityutil
{
   public static int dip2px(Context context,float dpvalue )
   {
      final float scale = context.getResources().getDisplayMetrics().density;
     return(int) (dpvalue * scale +0.5f);
     
   }

   public static int px2dip(Context context,float pxvalue)
  {
      final float scale = context.getResources().getDisplayMetrics().density;
      return (int) (pxvalue / scale +0.5f);
  }

}
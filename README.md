[![CSDN](https://img.shields.io/badge/CSDN-@xiaolongonly-blue.svg?style=flat)](http://blog.csdn.net/guoxiaolongonly)
[![CSDN](https://img.shields.io/badge/PersonBlog-@xiaolongonly-blue.svg?style=flat)](http://xiaolongonly.cn/)
[![API](https://img.shields.io/badge/API-16%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=16)


[![Screenshot of the sample app](https://github.com/xiaolongonly/PDFiumForAndroidDemo/blob/master/lucky.gif)](http://pre.im/b2h0)

**PDFiumForAndroidDemo** 是一个基于[pdfium](https://pdfium.googlesource.com/pdfium/)的library [AndroidPdfViewer](https://github.com/barteksc/AndroidPdfViewer)写的一个demo。

# 为什么写这个Demo?

当前流行的很多pdfViewer框架的封装，并不能支持定制化的界面，当交互和UI出完图的时候，你只能看着一堆框架懵逼。
如果你不知道你该选择哪个框架？在[AndroidPdf框架一览](http://blog.csdn.net/guoxiaolongonly/article/details/76992138)中会告诉你选择什么样的框架合适当前的需求。

# 怎么使用它？


```java

 mPDFManager = new PDFManager.Builder(this)
                .pdfFromFile(downLoadPdfFile)
				//or pdfFromStream()  pdfFromUri() pdfFormAsset() pdfFormByte()
				.setPassword()
                .setOnOpenErrorListener()
                .setOnOpenSuccessListener()
                .build();

```

正如上面所显示的，构建一个PDFManager只需要输入pdf的文件/流/字节码/资源文件位置/或者uri路径。PDFManager 提供了成功和失败的回调，并提供密码（如果pdf加密）。

然后你只需要获取你想要的pdf页面位置，还有图片的大小，就可以使用这张pdf页转成的图片了

```java

  mPDFManager.getPdfBitmapCustomSize(position, ScreenUtil.getScreenSize(context)[0] * 7 / 8)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onStart() {
                        ivLargeImage.setImageResource(R.mipmap.ic_launcher);
                    }

                    @Override
                    public void onCompleted() {
                        Log.d("adapterloadCompleted", "completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        ivLargeImage.setImageBitmap(bitmap);
                    }
                });

```

如果你没有更好的选择，那么尝试使用这个demo去做更好的封装吧。



# License

```

MIT License

```

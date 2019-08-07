import 'dart:io';

import 'package:flutter/material.dart';
import 'package:gallery_saver/gallery_saver.dart';
import 'package:image_picker/image_picker.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String firstButtonText = 'Take photo';
  String secondButtonText = 'Record video';
  double textSize = 20;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: Scaffold(
      body: Container(
        color: Colors.white,
        child: Column(
          children: <Widget>[
            Flexible(
              flex: 1,
              child: Container(
                child: SizedBox.expand(
                  child: RaisedButton(
                    color: Colors.blue,
                    onPressed: _takePhoto,
                    child: Text(firstButtonText,
                        style:
                            TextStyle(fontSize: textSize, color: Colors.white)),
                  ),
                ),
              ),
            ),
            Flexible(
              child: Container(
                  child: SizedBox.expand(
                child: RaisedButton(
                  color: Colors.white,
                  onPressed: _recordVideo,
                  child: Text(secondButtonText,
                      style: TextStyle(
                          fontSize: textSize, color: Colors.blueGrey)),
                ),
              )),
              flex: 1,
            )
          ],
        ),
      ),
    ));
  }

  void _takePhoto() async {
    ImagePicker.pickImage(source: ImageSource.camera)
        .then((File recordedImage) {
      if (recordedImage != null && recordedImage.path != null) {
        setState(() {
          firstButtonText = 'saving in progress...';
        });
        GallerySaver.saveImage(recordedImage.path).then((bool success) {
          setState(() {
            firstButtonText = 'image saved!';
          });
        });
      }
    });
  }

  void _recordVideo() async {
//    ImagePicker.pickVideo(source: ImageSource.camera)
//        .then((File recordedVideo) {
//      if (recordedVideo != null && recordedVideo.path != null) {
//        setState(() {
//          secondButtonText = 'saving in progress...';
//        });
//        GallerySaver.saveVideo(recordedVideo.path).then((bool success) {
//          setState(() {
//            secondButtonText = 'video saved!';
//          });
//        });
//      }
//    });
    String path = 'https://octopus-server-ireland-demo-influencer.s3.eu-west-1.amazonaws.com/p/bece03ae-9658-11e9-817d-e79e382c1c2e/e9e10994-b915-11e9-9076-5f4b84e1384c/008AADFD-91BD-4DBD-8E40-43BE0EF1D7A0_transcoded.MOV';
    GallerySaver.saveVideo(path).then((bool success) {
      setState(() {
        secondButtonText = 'video saved!';
      });
    });
  }
}

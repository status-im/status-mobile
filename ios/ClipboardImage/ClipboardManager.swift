//
//  ClipboardManager.swift
//  StatusIm
//
//  Created by Gheorghe on 03.11.2020.
//  Copyright © 2020 Status. All rights reserved.
//

import Foundation
import UIKit

@objc(MediaClipboard)
class MediaClipboard: NSObject {
  
  @objc(copyImage:resolver:rejecter:)
  func copyImage(_ base64Image: String,
                 resolver resolve: RCTPromiseResolveBlock,
                 rejecter reject: RCTPromiseRejectBlock) -> Void {
    if let data = Data.init(base64Encoded: base64Image, options:  Data.Base64DecodingOptions(rawValue: 0)), let image = UIImage(data: data) {
      UIPasteboard.general.image = image
      resolve(true)
    } else {
      reject("CANT_COPY_IMAGE", "The provided image could not be copied", nil)
    }
  }
  
  @objc(hasImages:rejecter:)
  func hasImages(_ resolve: RCTPromiseResolveBlock,
                 rejecter reject: RCTPromiseRejectBlock) -> Void {
    resolve(UIPasteboard.general.hasImages)
  }
  
  func createTempImage(image: UIImage) -> String? {
    let tempDirectoryURL = NSURL.fileURL(withPath: NSTemporaryDirectory(), isDirectory: true)
    
    let data = image.pngData()!
    let targetURL = tempDirectoryURL.appendingPathComponent("image.png")
    
    do {
      try data.write(to: targetURL)
      return targetURL.path
    } catch let error {
      NSLog("Unable to create file: \(error)")
    }
    
    return nil
  }
  
  @objc(paste:rejecter:)
  func paste(_ resolve: RCTPromiseResolveBlock,
             rejecter reject: RCTPromiseRejectBlock) -> Void {
    if let stringValue = UIPasteboard.general.string {
      resolve([
        "value": stringValue,
        "type": "text/plain"
      ])
    } else if let url = UIPasteboard.general.url {
      resolve([
        "value": url.absoluteString,
        "type": "text/plain"
      ])
    } else if let image = UIPasteboard.general.image {
      resolve([
        "value": createTempImage(image: image),
        "type": "image/png"
      ])
    } else {
      resolve([:])
    }
    
  }
}

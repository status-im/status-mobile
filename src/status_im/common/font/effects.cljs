(ns status-im.common.font.effects
  (:require
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    utils.image-server
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.font/get-font-file-for-initials-avatar
 (fn [callback]
   (utils.image-server/get-font-file-ready
    (if platform/ios?
      (:ios constants/initials-avatar-font-conf)
      (:android constants/initials-avatar-font-conf))
    callback)))

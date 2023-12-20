(ns status-im.common.font.effects
  (:require
    utils.image-server
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.font/get-font-file-for-initials-avatar
 (fn [callback]
   (utils.image-server/get-font-file-ready callback)))

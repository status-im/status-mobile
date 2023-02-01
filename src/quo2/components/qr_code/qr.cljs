(ns quo2.components.qr-code.qr
  (:require [react-native.core :as rn]))

(defn user-profile-qr-code
  [{:keys [key-uid public-key port qr-size]}]
  (let [profile-qr-url         (str "https://join.status.im/u/" public-key)
        base-64-qr-url         (js/btoa profile-qr-url)
        profile-image-type     "large"
        error-correction-level 4
        superimpose-profile?   true
        media-server-url       (str "https://localhost:"
                                    port
                                    "/GenerateQRCode?level="
                                    error-correction-level
                                    "&url="
                                    base-64-qr-url
                                    "&keyUid="
                                    key-uid
                                    "&allowProfileImage="
                                    superimpose-profile?
                                    "&size="
                                    qr-size
                                    "&imageName="
                                    profile-image-type)]
    [rn/view
     {:style {:flex-direction  :row
              :justify-content :center}}
     [rn/image
      {:source {:uri media-server-url}
       :style  {:width         qr-size
                :height        qr-size
                :border-radius 12}}]]))

(ns status-im2.contexts.scan.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.scan.style :as style]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [clojure.string :as string]
            [react-native.blur :as blur]
            [status-im.ui.components.list-selection :as list-selection]
            [utils.image-server :as image-server]
            [react-native.navigation :as navigation]))

(defn header
  []
  [:<>
   [quo/button
    {:icon                true
     :type                :blur-bg
     :size                32
     :accessibility-label :close-shell-scan-tab
     :override-theme      :dark
     :style               style/header-button
     :on-press            #(rf/dispatch [:navigate-back])}
    :i/close]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold 
     :style  style/header-heading}
    (i18n/label :t/scan)]
    ])

(defn abbreviated-url
  "The goal here is to generate a string that begins with
   join.status.im/u/ joined with the 1st 5 characters
   of the compressed public key followed by an ellipsis followed by
   the last 12 characters of the compressed public key"
  [base-url public-pk]
  (let [first-part-of-public-pk (subs public-pk 0 5)
        ellipsis                "..."
        public-pk-size          (count public-pk)
        last-part-of-public-pk  (subs public-pk (- public-pk-size 12) (- public-pk-size 1))
        abbreviated-url         (str base-url first-part-of-public-pk ellipsis last-part-of-public-pk)]
    abbreviated-url))

(defn profile-tab
  [window-width]
  (let [multiaccount    (rf/sub [:multiaccount])
        emoji-hash      (string/join (get multiaccount :emoji-hash))
        qr-size         (int (- window-width 64))
        public-pk       (get multiaccount :compressed-key)
        abbreviated-url (abbreviated-url image-server/status-profile-base-url-without-https public-pk)
        profile-url     (str image-server/status-profile-base-url public-pk)
        port            (rf/sub [:mediaserver/port])
        key-uid         (get multiaccount :key-uid)
        source-uri      (image-server/get-account-qr-image-uri {:key-uid    key-uid
                                                                :public-key public-pk
                                                                :port       port
                                                                :qr-size    qr-size})]
    [:<>
     [rn/view {:style style/qr-code-container}
      [quo/qr-code
       {:source {:uri source-uri}
        :width  qr-size
        :height qr-size}]
      
         ]

     
        ]))

(defn wallet-tab
  []
  [rn/text {:style style/wip-style} "not implemented"])

(defn tab-content
  [window-width]
  (let [selected-tab (reagent/atom :profile)]
    (fn []
      [:<>
       [header]
       [rn/view {:style style/tabs-container}
        [quo/segmented-control
         {:size           28
          :blur?          true
          :override-theme :dark
          :on-change      #(reset! selected-tab %)
          :default-active :profile
          :data           [{:id    :profile
                            :label (i18n/label :t/profile)}
                           
                            ]}]]
       [profile-tab window-width]
         ])))

(defn view
  []
  (let [window-width (rf/sub [:dimensions/window-width])]
    (fn []
      [rn/view
       {:flex        1
        :padding-top (navigation/status-bar-height)}
       [blur/view style/blur]
       [tab-content window-width]])))

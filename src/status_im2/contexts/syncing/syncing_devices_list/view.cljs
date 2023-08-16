(ns status-im2.contexts.syncing.syncing-devices-list.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.syncing.device.view :as device]
            [status-im2.contexts.syncing.syncing-devices-list.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [devices                                   (rf/sub [:pairing/installations])
        devices-with-button                       (map #(assoc % :show-button? true) devices)
        user-device                               (first devices-with-button)
        other-devices                             (rest devices-with-button)
        profile-color                             (rf/sub [:profile/customization-color])
        {:keys [paired-devices unpaired-devices]} (group-by
                                                   #(if (:enabled? %) :paired-devices :unpaired-devices)
                                                   other-devices)]
    [rn/view {:style style/container-main}
     [quo/page-nav
      {:type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])}]
     [rn/view {:style style/page-container}
      [rn/view {:style style/title-container}
       [quo/text
        {:size   :heading-1
         :weight :semi-bold
         :style  {:color colors/white}}
        (i18n/label :t/syncing)]
       [quo/button
        {:size                32
         :type                :primary
         :customization-color profile-color
         :icon-only?          true
         :on-press            #(rf/dispatch [:navigate-to :settings-setup-syncing])}
        :i/add]]
      [device/view (merge user-device {:this-device? true})]
      (when (seq paired-devices)
        [rn/view
         [quo/text
          {:size   :paragraph-2
           :weight :medium
           :style  style/subtitle}
          (i18n/label :t/paired-with-this-device)]
         [rn/flat-list
          {:key-fn    str
           :render-fn device/view
           :data      paired-devices}]])
      (when (seq unpaired-devices)
        [rn/view
         [quo/text
          {:size   :paragraph-2
           :weight :medium
           :style  style/subtitle}
          (i18n/label :t/not-paired-with-this-device)]
         [rn/flat-list
          {:key-fn    str
           :render-fn device/view
           :data      unpaired-devices}]])]]))

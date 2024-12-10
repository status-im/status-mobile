(ns status-im.contexts.syncing.syncing-devices-list.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.check-before-syncing.view :as check-before-syncing]
    [status-im.contexts.syncing.device.view :as device]
    [status-im.contexts.syncing.syncing-devices-list.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn go-back
  []
  (rf/dispatch [:navigate-back]))

(defn open-setup-syncing
  [customization-color]
  (rf/dispatch
   [:show-bottom-sheet
    {:theme :dark
     :shell? true
     :content
     (fn [] [check-before-syncing/view
             {:customization-color customization-color
              :on-submit           #(rf/dispatch [:open-modal :settings-setup-syncing])}])}]))

(defn view
  []
  (let [devices                                     (rf/sub [:pairing/installations])
        devices-with-button                         (map #(assoc % :show-button? true) devices)
        user-device                                 (first devices-with-button)
        other-devices                               (rest devices-with-button)
        profile-color                               (rf/sub [:profile/customization-color])
        open-setup-syncing-with-customization-color (rn/use-callback (partial open-setup-syncing
                                                                              profile-color)
                                                                     [profile-color])
        {:keys [paired-devices unpaired-devices]}   (group-by
                                                     #(if (:enabled? %)
                                                        :paired-devices
                                                        :unpaired-devices)
                                                     other-devices)
        keycard?                                    (rf/sub [:keycard/keycard-profile?])
        keycard-feature-unavailable                 (rn/use-callback
                                                     #(rf/dispatch [:keycard/feature-unavailable-show
                                                                    {:theme :dark}]))]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   go-back}]
     [rn/scroll-view
      {:content-container-style         style/page-container
       :style                           {:flex 1}
       :shows-vertical-scroll-indicator false}
      [rn/view {:style style/title-container}
       [quo/text
        {:size   :heading-1
         :weight :semi-bold
         :style  {:color colors/white}}
        (i18n/label :t/paired-devices)]
       [quo/button
        {:size                32
         :type                :primary
         :customization-color profile-color
         :icon-only?          true
         :on-press            (if keycard?
                                keycard-feature-unavailable
                                open-setup-syncing-with-customization-color)}
        :i/add]]
      [device/view (merge user-device {:this-device? true})]
      (when (seq paired-devices)
        [rn/view
         [quo/text
          {:size   :paragraph-2
           :weight :medium
           :style  style/subtitle}
          (i18n/label :t/paired-with-this-device)]
         (for [device-props paired-devices]
           ^{:key (:installation-id device-props)}
           [device/view device-props])])
      (when (seq unpaired-devices)
        [rn/view
         [quo/text
          {:size   :paragraph-2
           :weight :medium
           :style  style/subtitle}
          (i18n/label :t/not-paired-with-this-device)]
         (for [device-props unpaired-devices]
           ^{:key (:installation-id device-props)}
           [device/view device-props])])]]))

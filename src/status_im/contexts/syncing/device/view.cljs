(ns status-im.contexts.syncing.device.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.syncing.device.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [name
           installation-id
           this-device?
           device-type
           enabled?
           show-button?]}]
  (let [paired?       (and (not this-device?) enabled?)
        unpaired?     (not enabled?)
        unpair-device (fn []
                        (rf/dispatch [:syncing/disable-installation installation-id]))
        pair-device   (fn []
                        (rf/dispatch [:syncing/enable-installation installation-id]))]
    [quo/settings-item
     (cond->
       {:container-style style/device-container
        :title           name
        :blur?           true
        :image           :icon
        :image-props     (cond (#{:mobile :ios :android} (keyword device-type))
                               :i/mobile
                               :else :i/desktop)}
       (and show-button? unpaired?) (assoc
                                     :action :button
                                     :action-props
                                     {:button-text (i18n/label :t/pair)
                                      :on-press    pair-device})
       (and show-button? paired?)   (assoc
                                     :action :button
                                     :action-props
                                     {:button-text (i18n/label :t/unpair)
                                      :on-press    unpair-device})
       this-device?                 (assoc
                                     :tag       :positive
                                     :tag-props {:label (i18n/label :t/this-device)}))]))

(ns status-im.contexts.syncing.device.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.syncing.device.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- hide-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn- unpair-confirmation
  [installation-id name]
  (fn []
    (let [unpair-device #(rf/dispatch [:syncing/disable-installation installation-id])]
      [rn/view
       [quo/drawer-top
        {:title       (i18n/label :t/unpair-device)
         :description (i18n/label :t/unpair-device-description {:name name})}]
       [quo/bottom-actions
        {:actions          :two-actions
         :button-one-label (i18n/label :t/unpair)
         :button-one-props {:type     :danger
                            :on-press unpair-device}
         :button-two-label (i18n/label :t/cancel)
         :button-two-props {:type     :grey
                            :on-press hide-sheet}}]])))

(defn view
  [{:keys [name
           installation-id
           this-device?
           device-type
           enabled?
           show-button?]}]
  (let [paired?                  (and (not this-device?) enabled?)
        unpaired?                (not enabled?)
        show-unpair-confirmation (fn []
                                   (rf/dispatch [:show-bottom-sheet
                                                 {:theme   :dark
                                                  :content (unpair-confirmation installation-id name)}]))
        pair-device              (fn []
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
                                      :on-press    show-unpair-confirmation})
       this-device?                 (assoc
                                     :tag       :positive
                                     :tag-props {:label (i18n/label :t/this-device)}))]))

(ns status-im2.contexts.syncing.device.view
  [:require
   [quo2.core :as quo]
   [utils.i18n :as i18n]
   [status-im2.contexts.syncing.device.style :as style]])

(defn view
  [{:keys [name
           this-device?
           device-type
           enabled?
           show-button?]}]
  (let [paired?   (and (not this-device?) enabled?)
        unpaired? (not enabled?)]
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
                                     {:title    (i18n/label :t/pair)
                                      :on-press #(js/alert "feature not added yet")})
       (and show-button? paired?)   (assoc
                                     :action :button
                                     :action-props
                                     {:button-text (i18n/label :t/unpair)
                                      :on-press    #(js/alert "feature not added yet")})
       this-device?                 (assoc
                                     :tag       :positive
                                     :tag-props {:label (i18n/label :t/this-device)}))]))

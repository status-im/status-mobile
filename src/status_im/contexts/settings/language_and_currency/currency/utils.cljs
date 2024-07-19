(ns status-im.contexts.settings.language-and-currency.currency.utils)

(defn make-currency-item
  "This function generates props for quo/category component item"
  [{:keys [currency selected-currency on-change]}]
  (cond-> {:title             (if (:token? currency)
                                (:short-name currency)
                                (str (:short-name currency) " · " (:symbol currency)))
           :description       :text
           :description-props {:text (:name currency)}
           :image             (when (:token? currency) :token)
           :image-props       {:token (:id currency)
                               :size  :size-20}
           :action            :selector
           :action-props      {:type      :radio
                               :blur?     true
                               :checked?  (= selected-currency (:id currency))
                               :on-change #(on-change (:id currency))}}))

(ns status-im.contexts.settings.language-and-currency.currency.utils)

(defn make-currency-item
  "This function generates props for quo/category component item"
  [{:keys [currency selected-currency on-change]}]
  (cond-> {:title             (if (:token? currency)
                                (:code currency)
                                (str (:code currency) " · " (:symbol currency)))
           :description       :text
           :description-props {:text (:display-name currency)}
           :image             (when (:token? currency) :token)
           :image-props       {:token (:id currency)
                               :size  :size-20}
           :action            :selector
           :action-props      {:type      :checkbox
                               :blur?     true
                               :checked?  (= selected-currency (:id currency))
                               :on-change #(on-change (:id currency))}}))

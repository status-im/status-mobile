(ns status-im.contexts.settings.language-and-currency.currency.utils
  (:require [status-im.constants :as constants]))

(defn make-currency-item
  "This function generates props for quo/category component item"
  [{:keys [currency selected-currency on-change]}]
  {:title             (if (:token? currency)
                        (:short-name currency)
                        (str (:short-name currency) " · " (:symbol currency)))
   :description       :text
   :description-props {:text (:name currency)}
   :container-style   {:height constants/currency-item-height}
   :image             (when (:token? currency) :token)
   :image-props       {:token (:id currency)
                       :size  :size-20}
   :action            :selector
   :action-props      {:type      :radio
                       :blur?     true
                       :checked?  (= selected-currency (:id currency))
                       :on-change #(on-change (:id currency))}})

(ns status-im.contexts.wallet.send.transaction-details.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn sheet
  [_]
  [rn/view
   [quo/drawer-top
    {:title "Transaction settings"}]
   [quo/category
    {:list-type :settings
     :data      [{:title             "Normal ~60s"
                  :image             :icon
                  :image-props       :i/placeholder
                  :description       :text
                  :description-props {:text "€1.45"}
                  :on-press          #()
                  :action            :selector
                  :action-props      {:type :radio}
                  :label             :text
                  :preview-size      :size-32}
                 {:title             "Fast ~40s"
                  :image             :icon
                  :image-props       :i/placeholder
                  :description       :text
                  :description-props {:text "€1.65"}
                  :on-press          #()
                  :action            :selector
                  :action-props      {:type :radio}
                  :label             :text
                  :preview-size      :size-32}
                 {:title             "Urget ~15s"
                  :image             :icon
                  :image-props       :i/placeholder
                  :description       :text
                  :description-props {:text "€1.85"}
                  :on-press          #()
                  :action            :selector
                  :action-props      {:type :radio}
                  :label             :text
                  :preview-size      :size-32}]}]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-props {:on-press #()}
     :button-one-label (i18n/label :t/confirm)}]])


(ns status-im.contexts.wallet.send.transaction-settings.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))


(defn custom-settings-sheet
  [_]
  [rn/view
   [quo/drawer-top
    {:title "Custom"}]
   [quo/category
    {:list-type :settings
     :data      [{:title             "Max base fee"
                  :description-props {:text "8.2 GWEI - €1.45"}
                  :image             :none
                  :description       :text
                  :action            :arrow
                  :on-press          #()
                  :label             :text
                  :preview-size      :size-32}
                 {:title             "Priority fee"
                  :description-props {:text "0.06 GWEI - €0.03"}
                  :image             :none
                  :description       :text
                  :action            :arrow
                  :on-press          #()
                  :label             :text
                  :preview-size      :size-32}
                 {:title             "Gas amount"
                  :description-props {:text "31,500 UNITS"}
                  :image             :none
                  :description       :text
                  :action            :arrow
                  :on-press          #()
                  :label             :text
                  :preview-size      :size-32}
                 {:title             "Nonce"
                  :description-props {:text "22"}
                  :image             :none
                  :description       :text
                  :action            :arrow
                  :on-press          #()
                  :label             :text
                  :preview-size      :size-32}]}]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-props {:on-press #(rf/dispatch [:hide-bottom-sheet])}
     :button-one-label (i18n/label :t/confirm)}]])

(defn settings-sheet
  [_]
  (let [[selected-id set-selected-id] (rn/use-state :normal)]
    [rn/view
     [quo/drawer-top
      {:title "Transaction settings"}]
     [quo/category
      {:list-type :settings
       :data      [{:title             "Normal ~60s"
                    :image-props       "🍿"
                    :description-props {:text "€1.45"}
                    :image             :emoji
                    :description       :text
                    :action            :selector
                    :action-props      {:type     :radio
                                        :checked? (= :normal selected-id)}
                    :on-press          #(set-selected-id :normal)
                    :label             :text
                    :preview-size      :size-32}
                   {:title             "Fast ~40s"
                    :image-props       "🚗"
                    :description-props {:text "€1.65"}
                    :image             :emoji
                    :description       :text
                    :action            :selector
                    :action-props      {:type     :radio
                                        :checked? (= :fast selected-id)}
                    :on-press          #(set-selected-id :fast)
                    :label             :text
                    :preview-size      :size-32}
                   {:title             "Urgent ~15s"
                    :image-props       "🚀"
                    :description-props {:text "€1.85"}
                    :image             :emoji
                    :description       :text
                    :action            :selector
                    :action-props      {:type     :radio
                                        :checked? (= :urgent selected-id)}
                    :on-press          #(set-selected-id :urgent)
                    :label             :text
                    :preview-size      :size-32}
                   {:title             "Custom"
                    :image-props       :i/edit
                    :description-props {:text "Set your own fees and nonce"}
                    :image             :icon
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch
                                         [:show-bottom-sheet
                                          {:content custom-settings-sheet}])
                    :label             :text
                    :preview-size      :size-32}]}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press #(rf/dispatch [:hide-bottom-sheet])}
       :button-one-label (i18n/label :t/confirm)}]]))

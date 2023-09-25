(ns status-im2.contexts.quo-preview.notifications.toast
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.re-frame :as rf]))

(defn toast-button
  ([id opts] (toast-button id id opts))
  ([text id opts]
   (let [toast-opts (rf/sub [:toasts/toast id])
         dismiss!   #(rf/dispatch [:toasts/close id])
         toast!     #(rf/dispatch [:toasts/upsert (assoc opts :id id)])
         dismissed? (not toast-opts)]
     [rn/view {:style {:margin-bottom 10}}
      [quo/button
       {:size     32
        :on-press #(if dismissed? (toast!) (dismiss!))}
       (if dismissed? text (str "DISMISS " text))]])))

(defn toast-button-basic
  []
  [toast-button
   "Toast: basic"
   {:icon :placeholder :icon-color "white" :text "This is an example toast"}])

(defn toast-button-with-undo-action
  []
  [toast-button
   "Toast: with undo action"
   {:icon          :info
    :icon-color    colors/danger-50-opa-40
    :text          "This is an example toast"
    :duration      4000
    :undo-duration 4
    :undo-on-press #(do
                      (rf/dispatch [:toasts/upsert
                                    {:icon       :i/correct
                                     :icon-color colors/success-50-opa-40
                                     :text       "Undo pressed"}])
                      (rf/dispatch [:toasts/close
                                    "Toast: with undo action"]))}])

(defn toast-button-multiline
  []
  [toast-button
   "Toast: multiline"
   {:icon :correct
    :icon-color colors/success-50-opa-40
    :text
    "This is an example multiline toast This is an example multiline toast This is an example multiline toast"
    :undo-duration 4
    :undo-on-press
    #(do
       (rf/dispatch
        [:toasts/upsert
         {:icon :i/correct :icon-color colors/success-50-opa-40 :text "Undo pressed"}])
       (rf/dispatch [:toasts/close "Toast: with undo action"]))}])

(defn toast-button-30s-duration
  []
  [toast-button
   "Toast: 30s duration"
   {:icon       :correct
    :icon-color colors/success-50-opa-40
    :text       "This is an example toast"
    :duration   30000}])

(defn update-toast-button
  []
  (let [suffix (reagent/atom 0)]
    (fn []
      (let [toast-opts (rf/sub [:toasts/toast "Toast: 30s duration"])]
        (when toast-opts
          [rn/view {:style {:margin-bottom 10}}
           [quo/button
            {:size 32
             :on-press
             #(rf/dispatch
               [:toasts/upsert
                {:id         "Toast: 30s duration"
                 :icon       :i/info
                 :icon-color colors/danger-50-opa-40
                 :text       (str "This is an updated example toast" " - " (swap! suffix inc))
                 :duration   3000}])}
            "update above toast"]])))))

(defn preview-toasts
  []
  (fn []
    [preview/preview-container
     [rn/view
      {:background-color "#508485"
       :flex-direction   :column
       :justify-content  :flex-start
       :height           300}]
     [into
      [rn/view
       {:flex    1
        :padding 16}]
      [^{:key :basic} [toast-button-basic]
       ^{:key :with-undo-action} [toast-button-with-undo-action]
       ^{:key :with-multiline} [toast-button-multiline]
       ^{:key :30s-duration} [toast-button-30s-duration]
       ^{:key :upsert}
       [update-toast-button]]]]))

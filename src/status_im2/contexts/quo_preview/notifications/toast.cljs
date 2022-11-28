(ns status-im2.contexts.quo-preview.notifications.toast
  (:require
   [quo2.components.buttons.button :as button]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]
   [reagent.core :as reagent]
   [utils.re-frame :as re-frame]))

(def descriptor
  [{:label   "Icon"
    :key     :icon
    :type    :select
    :options [{:key   :placeholder
               :value :placeholder}
              {:key   :checkmark_circle
               :value :checkmark_circle}]}
   {:label "text"
    :key   :text
    :type  :text}
   {:label   "Action"
    :key     :action
    :type    :select
    :options [{:key   :undo
               :value :undo}]}])

(def toasts-opts {"Toast: basic"             {:icon :placeholder :icon-color "green" :text "This is an example toast"}
                  "Toast: with undo action"  {:icon          :info
                                              :icon-color    colors/danger-50-opa-40
                                              :text          "This is an example toast"
                                              :duration      4000
                                              :undo-duration 4
                                              :undo-on-press #(do
                                                                (re-frame/dispatch [:toasts/create {:icon :placeholder :icon-color "green" :text "Undo pressed"}])
                                                                (re-frame/dispatch [:toasts/close "Toast: with undo action"]))}
                  "Toast: 30s duration"      {:icon :placeholder :icon-color "green" :text "This is an example toast" :duration 30000}
                  "Toast: update prev toast" {:icon :placeholder :icon-color "red" :text "This is an example toast" :duration 30000}})

(defn toast-button [id opts]
  (let [dismissed? (reagent/atom true)
        dismiss!   #(re-frame/dispatch [:toasts/close id])
        toast!     (fn []
                     (reset! dismissed? false)
                     (re-frame/dispatch [:toasts/upsert id (assoc opts :on-dismissed #(reset! dismissed? true))]))]
    (fn []
      [rn/view {:style {:margin-bottom 10}}
       [button/button
        {:size     32
         :on-press #(if @dismissed?
                      (toast!)
                      (dismiss!))}
        (if @dismissed? id (str "DISMISS " id))]])))

(defn preview
  []
  (fn []
    [rn/view
     [rn/view
      {:background-color "#508485"
       :flex-direction   :column
       :justify-content  :flex-start
       :height           500
       :padding-vertical 30}]
     [into
      [rn/view
       {:flex    1
        :padding 16}
       (map (fn [[id opts]] ^{:key id} [vector toast-button id opts]) toasts-opts)]]]))

(defn preview-toasts
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex   1
     :header [preview]
     :key-fn str
     :keyboardShouldPersistTaps :always}]])

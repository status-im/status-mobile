(ns status-im2.contexts.quo-preview.notifications.toast
  (:require
   [quo2.components.buttons.button :as button]
   [react-native.core :as rn]
   [reagent.core      :as reagent]
   [status-im2.contexts.quo-preview.preview :as preview]
   [utils.re-frame    :as re-frame]))

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

(defn preview
  []
  (let [state          (reagent/atom {:action :undo
                                      :icon   :placeholder
                                      :text   "You can only add 6 photos"})
        toast-dismissed? (reagent/atom true)
        unmount-toast! #(re-frame/dispatch [:toasts/close "toast-1"])
        toast!         (fn [props]
                         (re-frame/dispatch
                          [:toasts/upsert "toast-1"
                           (assoc props :on-dismissed #(reset! toast-dismissed? true))])
                         (reset! toast-dismissed? false))]
    (fn []
      (let [props @state]
        [rn/view
         [rn/view
          {:background-color "#508485"
           :flex-direction   :column
           :justify-content  :flex-start
           :height           500
           :padding-vertical 30}]
         [rn/view
          {:flex    1
           :padding 16}
          (when @toast-dismissed?
            [button/button
             {:size     32
              :on-press #(toast! props)}
             "Toast!"])

          (when-not @toast-dismissed?
            [button/button
             {:size     32
              :on-press #(unmount-toast!)}
             "No Toast!"])
          [preview/customizer state descriptor]]]))))

(defn preview-toasts
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex   1
     :header [preview]
     :key-fn str
     :keyboardShouldPersistTaps :always}]])

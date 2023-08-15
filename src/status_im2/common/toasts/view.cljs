(ns status-im2.common.toasts.view
  (:require [quo2.core :as quo]
            [react-native.background-timer :as background-timer]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im2.common.toasts.animation :as animation]
            [status-im2.common.toasts.style :as style]
            [utils.re-frame :as rf]))

(defn toast
  [toast-id]
  (let [{:keys [type user-public-key] :as toast-opts} (rf/sub [:toasts/toast toast-id])
        profile-picture                               (when user-public-key
                                                        (rf/sub [:chats/photo-path user-public-key]))
        toast-opts-with-profile-picture               (if profile-picture
                                                        (assoc-in toast-opts
                                                         [:user :profile-picture]
                                                         profile-picture)
                                                        toast-opts)]
    (if (= type :notification)
      [quo/notification toast-opts-with-profile-picture]
      [quo/toast toast-opts-with-profile-picture])))

(defn f-container
  [toast-id]
  (let [dismissed-locally?    (reagent/atom false)
        set-dismissed-locally #(reset! dismissed-locally? true)
        close-toast           #(rf/dispatch [:toasts/close toast-id])
        timer                 (reagent/atom nil)
        clear-timer           #(background-timer/clear-timeout @timer)]
    (fn []
      (let [duration     (or (rf/sub [:toasts/toast-cursor toast-id :duration]) 3000)
            on-dismissed (or (rf/sub [:toasts/toast-cursor toast-id :on-dismissed]) identity)
            create-timer #(reset! timer (background-timer/set-timeout close-toast duration))
            translate-y  (reanimated/use-shared-value 0)
            pan-gesture  (animation/pan-gesture {:clear-timer           clear-timer
                                                 :create-timer          create-timer
                                                 :translate-y           translate-y
                                                 :close-toast           close-toast
                                                 :set-dismissed-locally set-dismissed-locally
                                                 :dismissed-locally?    @dismissed-locally?})]
        ;; create auto dismiss timer, clear timer when unmount or duration changed
        (rn/use-effect (fn [] (create-timer) clear-timer) [duration])
        (rn/use-unmount #(on-dismissed toast-id))

        [gesture/gesture-detector {:gesture pan-gesture}
         [reanimated/view
          {:entering animation/slide-in-up-animation
           :exiting  animation/slide-out-up-animation
           :layout   animation/linear-transition
           :style    (reanimated/apply-animations-to-style
                      {:transform [{:translateY translate-y}]}
                      style/each-toast-container)}
          [toast toast-id]]]))))

(defn toasts
  []
  (->> (rf/sub [:toasts])
       :ordered
       (into [rn/view {:style style/outmost-transparent-container}]
             (map #(with-meta [:f> f-container %] {:key %})))))

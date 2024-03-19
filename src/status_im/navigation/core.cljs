(ns status-im.navigation.core
  (:require
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.navigation :as navigation]
    [status-im.common.theme.core :as theme]
    [status-im.navigation.effects :as effects]
    [status-im.navigation.options :as options]
    [status-im.navigation.state :as state]
    [status-im.navigation.view :as views]
    [utils.re-frame :as rf]))

(defn init
  []
  (navigation/set-lazy-component-registrator
   (fn [screen-key]
     (let [screen (views/screen screen-key)]
       (navigation/register-component screen-key
                                      (fn [] (gesture/gesture-handler-root-hoc screen))
                                      (fn [] screen)))))

  ;; APP LAUNCHED
  (navigation/reg-app-launched-listener
   (fn []
     (navigation/set-default-options options/default-options)
     (reset! state/curr-modal false)
     (reset! state/dissmissing false)
     (re-frame/dispatch [:bottom-sheet-hidden])
     (if (= @state/root-id :multiaccounts-stack)
       (re-frame/dispatch-sync [:set-multiaccount-root])
       (when @state/root-id
         (reset! theme/device-theme (rn/get-color-scheme))
         (re-frame/dispatch [:init-root @state/root-id])
         (re-frame/dispatch [:chat/check-last-chat])))
     (rn/hide-splash-screen)))

  (navigation/reg-component-did-appear-listener
   (fn [view-id]
     (let [view-id-with-prefix (keyword (str "screen/" (name view-id)))
           view                (or (get views/screens view-id)
                                   (get views/screens view-id-with-prefix))
           view-id             (:name view)]
       (when view
         (effects/set-view-id view-id)
         (when-not @state/curr-modal
           (reset! state/pushed-screen-id view-id))))))

  ;;;; Modal

  (navigation/reg-button-pressed-listener
   (fn [id]
     (if (= "dismiss-modal" id)
       (do
         (when-let [event (get-in views/screens [(last @state/modals) :on-dissmiss])]
           (rf/dispatch event))
         (effects/dismiss-modal))
       (when-let [handler (get-in views/screens [(keyword id) :right-handler])]
         (handler)))
     (when (= "legacy-back-button" id)
       (rf/dispatch [:navigate-back]))))

  (navigation/reg-modal-dismissed-listener
   (fn []
     (if (> (count @state/modals) 1)
       (let [new-modals (butlast @state/modals)]
         (reset! state/modals (vec new-modals))
         (effects/set-view-id (last new-modals)))
       (do
         (reset! state/modals [])
         (reset! state/curr-modal false)
         (effects/set-view-id @state/pushed-screen-id)))

     (let [component @state/dissmissing]
       (reset! state/dissmissing false)
       (when (keyword? component)
         (effects/open-modal component)))))

  ;;;; Toast

  (navigation/register-component
   "toasts"
   ; `:flex 0` is the same as `flex: 0 0 auto` in CSS.
   ; We need this to override the HOC default layout which is
   ; flex 1. If we don't override this property, this HOC
   ; will catch all touches/gestures while the toast is shown,
   ; preventing the user doing any action in the app
   #(gesture/gesture-handler-root-hoc views/toasts
                                      #js {:flex 0})
   (fn [] views/toasts))

  ;;;; Bottom sheet

  (navigation/register-component
   "bottom-sheet"
   (fn [] (gesture/gesture-handler-root-hoc views/bottom-sheet))
   (fn [] views/bottom-sheet))

  ;;;; Alert Banner
  (navigation/register-component
   "alert-banner"
   (fn [] (gesture/gesture-handler-root-hoc views/alert-banner #js {:flex 0}))
   (fn [] views/alert-banner))

  ;;;; LEGACY (should be removed in status 2.0)
  (navigation/register-component
   "bottom-sheet-old"
   (fn [] (gesture/gesture-handler-root-hoc views/sheet-comp-old))
   (fn [] views/sheet-comp-old))

  (navigation/register-component
   "popover"
   (fn [] (gesture/gesture-handler-root-hoc views/popover-comp))
   (fn [] views/popover-comp)))

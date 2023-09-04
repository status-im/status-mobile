(ns status-im2.navigation.core
  (:require [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.navigation :as navigation]
            [status-im2.navigation.roots :as roots]
            [status-im2.navigation.state :as state]
            [status-im2.navigation.view :as views]
            [taoensso.timbre :as log]
            [status-im2.common.theme.core :as theme]
            [status-im2.navigation.options :as options]))

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

(defn set-view-id
  [view-id]
  (when (get views/screens view-id)
    (re-frame/dispatch [:set-view-id view-id])))

(re-frame/reg-fx
 :set-view-id-fx
 (fn [view-id]
   (re-frame/dispatch [:screens/on-will-focus view-id])
   (when-let [{:keys [on-focus]} (get views/screens view-id)]
     (when on-focus
       (re-frame/dispatch on-focus)))))

(navigation/reg-component-did-appear-listener
 (fn [view-id]
   (when (get views/screens view-id)
     ;;NOTE when back from the background on Android, this event happens for all screens, but we need
     ;;only for active one
     (when (and @state/curr-modal (= @state/curr-modal view-id))
       (set-view-id view-id))
     (when-not @state/curr-modal
       (set-view-id view-id)
       (reset! state/pushed-screen-id view-id)))))

(defn dissmissModal
  ([] (dissmissModal nil))
  ([comp-id]
   (reset! state/dissmissing true)
   (navigation/dismiss-modal (name (or comp-id (last @state/modals))))))

(defn dismiss-all-modals
  []
  (when @state/curr-modal
    (reset! state/curr-modal false)
    (reset! state/dissmissing true)
    (doseq [modal @state/modals]
      (navigation/dismiss-modal (name modal)))
    (reset! state/modals [])))

;; ROOT
(re-frame/reg-fx
 :set-root
 (fn [root-id]
   (let [root (get (roots/roots) root-id)]
     (dismiss-all-modals)
     (re-frame/dispatch [:multiaccounts.ui/switch-theme
                         (get roots/themes root-id)
                         root-id])
     (reset! state/root-id (or (get-in root [:root :stack :id]) root-id))
     (navigation/set-root root))))

;; NAVIGATE-TO
(defn navigate
  [component]
  (let [{:keys [options]} (get views/screens component)]
    (dismiss-all-modals)
    (navigation/push
     (name @state/root-id)
     {:component {:id      component
                  :name    component
                  :options (merge (options/default-root)
                                  (options/statusbar-and-navbar)
                                  options
                                  (if (:topBar options)
                                    (options/merge-top-bar (options/topbar-options) options)
                                    {:topBar {:visible false}}))}})))

;; NAVIGATE-TO-WITHIN-STACK
(defn navigate-to-within-stack
  [[component comp-id]]
  (let [{:keys [options]} (get views/screens component)]
    (navigation/push
     (name comp-id)
     {:component {:id      component
                  :name    component
                  :options (merge (options/statusbar-and-navbar)
                                  options
                                  (if (:topBar options)
                                    (options/merge-top-bar (options/topbar-options) options)
                                    {:topBar {:visible false}}))}})))

(re-frame/reg-fx :navigate-to navigate)

(re-frame/reg-fx :navigate-to-within-stack navigate-to-within-stack)

(re-frame/reg-fx :navigate-replace-fx
                 (fn [view-id]
                   (navigation/pop (name @state/root-id))
                   (navigate view-id)))

(re-frame/reg-fx :navigate-back
                 (fn []
                   (if @state/curr-modal
                     (dissmissModal)
                     (navigation/pop (name @state/root-id)))))

(re-frame/reg-fx :navigate-back-within-stack
                 (fn [comp-id]
                   (navigation/pop (name comp-id))))

(re-frame/reg-fx :navigate-back-to
                 (fn [comp-id]
                   (navigation/pop-to (name comp-id))))

(re-frame/reg-fx :dismiss-modal
                 (fn [comp-id]
                   (dissmissModal (name comp-id))))

(defn pop-to-root
  [root-id]
  (navigation/pop-to-root root-id)
  (dismiss-all-modals))

(re-frame/reg-fx :pop-to-root-fx pop-to-root)

;; MODAL
(defn open-modal
  [component]
  (let [{:keys [options]} (get views/screens component)
        sheet?            (:sheet? options)]
    (if @state/dissmissing
      (reset! state/dissmissing component)
      (do
        (reset! state/curr-modal true)
        (swap! state/modals conj component)
        (navigation/show-modal
         {:stack {:children [{:component
                              {:name    component
                               :id      component
                               :options (merge (options/default-root)
                                               (options/statusbar-and-navbar)
                                               options
                                               (when sheet?
                                                 options/sheet-options))}}]}})))))

(re-frame/reg-fx :open-modal-fx open-modal)

(navigation/reg-button-pressed-listener
 (fn [id]
   (if (= "dismiss-modal" id)
     (do
       (when-let [event (get-in views/screens [(last @state/modals) :on-dissmiss])]
         (re-frame/dispatch event))
       (dissmissModal))
     (when-let [handler (get-in views/screens [(keyword id) :right-handler])]
       (handler)))))

(navigation/reg-modal-dismissed-listener
 (fn []
   (if (> (count @state/modals) 1)
     (let [new-modals (butlast @state/modals)]
       (reset! state/modals (vec new-modals))
       (set-view-id (last new-modals)))
     (do
       (reset! state/modals [])
       (reset! state/curr-modal false)
       (set-view-id @state/pushed-screen-id)))

   (let [component @state/dissmissing]
     (reset! state/dissmissing false)
     (when (keyword? component)
       (open-modal component)))))

;; OVERLAY
(def dissmiss-overlay navigation/dissmiss-overlay)

(defn show-overlay
  ([component] (show-overlay component {}))
  ([component opts]
   (dissmiss-overlay component)
   (navigation/show-overlay
    {:component {:name    component
                 :id      component
                 :options (merge (options/statusbar)
                                 {:layout  {:componentBackgroundColor :transparent
                                            :orientation              ["portrait"]}
                                  :overlay {:interceptTouchOutside true}}
                                 opts)}})))

;; toast
(navigation/register-component "toasts"
                               ; `:flex 0` is the same as `flex: 0 0 auto` in CSS.
                               ; We need this to override the HOC default layout which is
                               ; flex 1. If we don't override this property, this HOC
                               ; will catch all touches/gestures while the toast is shown,
                               ; preventing the user doing any action in the app
                               #(gesture/gesture-handler-root-hoc views/toasts
                                                                  #js {:flex 0})
                               (fn [] views/toasts))

(re-frame/reg-fx :show-toasts
                 (fn []
                   (show-overlay "toasts"
                                 {:overlay {:interceptTouchOutside false}
                                  :layout  {:componentBackgroundColor :transparent
                                            :orientation              ["portrait"]}})))
(re-frame/reg-fx :hide-toasts (fn [] (dissmiss-overlay "toasts")))

;; bottom sheet
(navigation/register-component "bottom-sheet"
                               (fn [] (gesture/gesture-handler-root-hoc views/bottom-sheet))
                               (fn [] views/bottom-sheet))

(re-frame/reg-fx :show-bottom-sheet (fn [] (show-overlay "bottom-sheet")))
(re-frame/reg-fx :hide-bottom-sheet (fn [] (dissmiss-overlay "bottom-sheet")))

;; MERGE OPTIONS
(re-frame/reg-fx
 :merge-options
 (fn [{:keys [id options]}]
   (navigation/merge-options id options)))

;; LEGACY (should be removed in status 2.0)
(defn get-screen-component
  [component]
  (let [{:keys [options]} (get views/screens component)]
    {:component {:id      component
                 :name    component
                 :options (merge (options/statusbar-and-navbar)
                                 options
                                 (options/merge-top-bar (options/topbar-options) options))}}))

(re-frame/reg-fx
 :set-stack-root-fx
 (fn [[stack component]]
   ;; We don't have bottom tabs as separate stacks anymore,. So the old way of pushing screens in
   ;; specific tabs will not work. Disabled set-stack-root for :shell-stack as it is not working
   ;; and currently only being used for browser and some rare keycard flows after login
   (when-not (= @state/root-id :shell-stack)
     (log/debug :set-stack-root-fx stack component)
     (navigation/set-stack-root
      (name stack)
      (if (vector? component)
        (mapv get-screen-component component)
        (get-screen-component component))))))

(re-frame/reg-fx :show-popover (fn [] (show-overlay "popover")))
(re-frame/reg-fx :hide-popover (fn [] (dissmiss-overlay "popover")))
(re-frame/reg-fx :show-visibility-status-popover
                 (fn [] (show-overlay "visibility-status-popover")))
(re-frame/reg-fx :hide-visibility-status-popover
                 (fn [] (dissmiss-overlay "visibility-status-popover")))
(re-frame/reg-fx :show-bottom-sheet-overlay-old (fn [] (show-overlay "bottom-sheet-old")))
(re-frame/reg-fx :dismiss-bottom-sheet-overlay-old (fn [] (dissmiss-overlay "bottom-sheet-old")))
(re-frame/reg-fx :show-wallet-connect-sheet (fn [] (show-overlay "wallet-connect-sheet")))
(re-frame/reg-fx :hide-wallet-connect-sheet (fn [] (dissmiss-overlay "wallet-connect-sheet")))
(re-frame/reg-fx :show-wallet-connect-success-sheet
                 (fn [] (show-overlay "wallet-connect-success-sheet")))
(re-frame/reg-fx :hide-wallet-connect-success-sheet
                 (fn [] (dissmiss-overlay "wallet-connect-success-sheet")))
(re-frame/reg-fx :show-wallet-connect-app-management-sheet
                 (fn [] (show-overlay "wallet-connect-app-management-sheet")))
(re-frame/reg-fx :hide-wallet-connect-app-management-sheet
                 (fn [] (dissmiss-overlay "wallet-connect-app-management-sheet")))
(re-frame/reg-fx :show-signing-sheet (fn [] (show-overlay "signing-sheet")))
(re-frame/reg-fx :hide-signing-sheet (fn [] (dissmiss-overlay "signing-sheet")))
(re-frame/reg-fx :show-select-acc-sheet (fn [] (show-overlay "select-acc-sheet")))
(re-frame/reg-fx :hide-select-acc-sheet (fn [] (dissmiss-overlay "select-acc-sheet")))

(defonce
  _
  [(navigation/register-component
    "popover"
    (fn [] (gesture/gesture-handler-root-hoc views/popover-comp))
    (fn [] views/popover-comp))

   (navigation/register-component
    "visibility-status-popover"
    (fn [] (gesture/gesture-handler-root-hoc views/visibility-status-popover-comp))
    (fn [] views/visibility-status-popover-comp))

   (navigation/register-component
    "bottom-sheet-old"
    (fn [] (gesture/gesture-handler-root-hoc views/sheet-comp-old))
    (fn [] views/sheet-comp-old))

   (navigation/register-component
    "wallet-connect-sheet"
    (fn [] (gesture/gesture-handler-root-hoc views/wallet-connect-comp))
    (fn [] views/wallet-connect-comp))

   (navigation/register-component
    "wallet-connect-success-sheet"
    (fn [] (gesture/gesture-handler-root-hoc views/wallet-connect-success-comp))
    (fn [] views/wallet-connect-success-comp))

   (navigation/register-component
    "wallet-connect-app-management-sheet"
    (fn [] (gesture/gesture-handler-root-hoc views/wallet-connect-app-management-comp))
    (fn [] views/wallet-connect-app-management-comp))

   (navigation/register-component
    "signing-sheet"
    (fn [] (gesture/gesture-handler-root-hoc views/signing-comp))
    (fn [] views/signing-comp))

   (navigation/register-component
    "select-acc-sheet"
    (fn [] (gesture/gesture-handler-root-hoc views/select-acc-comp))
    (fn [] views/select-acc-comp))])

(ns status-im2.navigation.core
  (:require [quo2.foundations.colors :as colors]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.navigation :as navigation]
            [react-native.platform :as platform]
            [status-im.multiaccounts.login.core :as login-core]
            [status-im2.navigation.roots :as roots]
            [status-im2.navigation.state :as state]
            [status-im2.navigation.view :as views]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

;; REGISTER COMPONENT (LAZY)
(defn reg-comp
  [key]
  (log/debug "reg-comp" key)
  (if-let [comp (get views/components (keyword key))]
    (navigation/register-component key (fn [] (views/component comp)) nil)
    (let [screen (views/screen key)]
      (navigation/register-component key
                                     (fn [] (gesture/gesture-handler-root-hoc screen))
                                     (fn [] screen)))))

(defn dismiss-all-modals
  []
  (log/debug "dissmiss-all-modals"
             {:curr-modal  @state/curr-modal
              :modals      @state/modals
              :dissmissing @state/dissmissing})
  (when @state/curr-modal
    (reset! state/curr-modal false)
    (reset! state/dissmissing true)
    (doseq [modal @state/modals]
      (navigation/dismiss-modal (name modal)))
    (reset! state/modals [])))

(defn status-bar-options
  []
  (if platform/android?
    {:navigationBar {:backgroundColor (colors/theme-colors colors/white colors/neutral-100)}
     :statusBar     {:translucent     true
                     :backgroundColor :transparent
                     :drawBehind      true
                     :style           (if (colors/dark?) :light :dark)}}
    {:statusBar {:style (if (colors/dark?) :light :dark)}}))

;; PUSH SCREEN TO THE CURRENT STACK
(defn navigate
  [comp]
  (log/debug "NAVIGATE" comp)
  (let [{:keys [options]} (get views/screens comp)]
    (navigation/push
     (name @state/root-comp-id)
     {:component {:id      comp
                  :name    comp
                  :options (merge (status-bar-options)
                                  options
                                  (roots/merge-top-bar (roots/topbar-options) options))}})
    ;;if we push the screen from modal, we want to dismiss all modals
    (dismiss-all-modals)))

;; OPEN MODAL
(defn update-modal-topbar-options
  [options]
  (log/debug "update-modal-topbar-options" options)
  (merge options
         ;; TODO fix colors and icons from quo2 later if needed
         #_(roots/merge-top-bar {:elevation       0
                                 :noBorder        true
                                 :title           {:color quo.colors/black}
                                 :background      {:color quo.colors/white}
                                 :leftButtonColor quo.colors/black
                                 :leftButtons     {:id   "dismiss-modal"
                                                   :icon (icons/icon-source :main-icons/close)}}
                                options)))

(defn open-modal
  [comp]
  (log/debug "open-modal"
             {:comp        comp
              :dissmissing @state/dissmissing
              :curr-modal  @state/curr-modal
              :modals      @state/modals})
  (let [{:keys [options]} (get views/screens comp)]
    (if @state/dissmissing
      (reset! state/dissmissing comp)
      (do
        (reset! state/curr-modal true)
        (swap! state/modals conj comp)
        (navigation/show-modal
         {:stack {:children
                  [{:component
                    {:name    comp
                     :id      comp
                     :options (update-modal-topbar-options
                               (merge (roots/status-bar-options)
                                      (roots/default-root)
                                      options))}}]}})))))

(re-frame/reg-fx :open-modal-fx open-modal)

;; DISSMISS MODAL
(defn dissmissModal
  []
  (log/debug "dissmissModal")
  (reset! state/dissmissing true)
  (navigation/dismiss-modal (name (last @state/modals))))

(defn button-pressed-listener
  [id]
  (if (= "dismiss-modal" id)
    (do
      (when-let [event (get-in views/screens [(last @state/modals) :on-dissmiss])]
        (re-frame/dispatch event))
      (dissmissModal))
    (when-let [handler (get-in views/screens [(keyword id) :right-handler])]
      (handler))))

(defn set-view-id
  [view-id]
  (log/debug "set-view-id" view-id)
  (when-let [{:keys [on-focus]} (get views/screens view-id)]
    (re-frame/dispatch [:set-view-id view-id])
    (re-frame/dispatch [:screens/on-will-focus view-id])
    (when on-focus
      (re-frame/dispatch on-focus))))

(defn modal-dismissed-listener
  []
  (log/debug "modal-dismissed"
             {:modals      @state/modals
              :curr-modal  @state/curr-modal
              :dissmissing @state/dissmissing})
  (if (> (count @state/modals) 1)
    (let [new-modals (butlast @state/modals)]
      (reset! state/modals (vec new-modals))
      (set-view-id (last new-modals)))
    (do
      (reset! state/modals [])
      (reset! state/curr-modal false)
      (set-view-id @state/pushed-screen-id)))

  (let [comp @state/dissmissing]
    (reset! state/dissmissing false)
    (when (keyword? comp)
      (open-modal comp))))

;; SCREEN DID APPEAR
(defn component-did-appear-listener
  [view-id]
  (log/debug "screen-appear-reg" view-id)
  (when (get views/screens view-id)
    (when (and (not= view-id :bottom-sheet)
               (not= view-id :toasts)
               (not= view-id :popover)
               (not= view-id :visibility-status-popover))
      (set-view-id view-id)
      (when-not @state/curr-modal
        (reset! state/pushed-screen-id view-id)))))

;; SCREEN DID DISAPPEAR
(defn component-did-disappear-listener
  [_]
  #_(when-not (#{:popover :bottom-sheet :signing-sheet :visibility-status-popover :wallet-connect-sheet
                 :wallet-connect-success-sheet :wallet-connect-app-management-sheet}
               view-id)
      ;; TODO what to do ?
      (doseq [[_ {:keys [ref value]}] @quo.text-input/text-input-refs]
        (.setNativeProps ^js ref (clj->js {:text value})))
      (doseq [[^js text-input default-value] @react/text-input-refs]
        (.setNativeProps text-input (clj->js {:text default-value})))))

;; APP LAUNCHED
(defn app-launched-listener
  []
  (reset! state/curr-modal false)
  (reset! state/dissmissing false)
  (if (or (= @state/root-id :multiaccounts)
          (= @state/root-id :multiaccounts-keycard))
    (re-frame/dispatch-sync [::set-multiaccount-root])
    (when @state/root-id
      (reset! state/root-comp-id @state/root-id)
      (navigation/set-root (get (roots/roots) @state/root-id))
      (re-frame/dispatch [::login-core/check-last-chat])))
  (rn/hide-splash-screen))

;; SET ROOT
(re-frame/reg-fx
 :init-root-fx
 (fn [new-root-id]
   (log/debug :init-root-fx new-root-id)
   (dismiss-all-modals)
   (reset! state/root-comp-id new-root-id)
   (reset! state/root-id @state/root-comp-id)
   (navigation/set-root (get (roots/roots) new-root-id))))

(re-frame/reg-fx
 :init-root-with-component-fx
 (fn [[new-root-id new-root-comp-id]]
   (log/debug :init-root-with-component-fx new-root-id new-root-comp-id)
   (dismiss-all-modals)
   (reset! state/root-comp-id new-root-comp-id)
   (reset! state/root-id @state/root-comp-id)
   (navigation/set-root (get (roots/roots) new-root-id))))

(rf/defn set-multiaccount-root
  {:events [::set-multiaccount-root]}
  [{:keys [db]}]
  (log/debug :set-multiaccounts-root)
  (let [key-uid          (get-in db [:multiaccounts/login :key-uid])
        keycard-account? (boolean (get-in db
                                          [:multiaccounts/multiaccounts
                                           key-uid
                                           :keycard-pairing]))]
    {:init-root-fx (if keycard-account? :multiaccounts-keycard :multiaccounts)}))

(defn get-screen-component
  [comp]
  (log/debug :get-screen-component comp)
  (let [{:keys [options]} (get views/screens comp)]
    {:component {:id      comp
                 :name    comp
                 :options (merge (roots/status-bar-options)
                                 options
                                 (roots/merge-top-bar (roots/topbar-options) options))}}))

;; SET STACK ROOT
(re-frame/reg-fx
 :set-stack-root-fx
 (fn [[stack comp]]
   (log/debug :set-stack-root-fx stack comp)
   (navigation/set-stack-root
    (name stack)
    (if (vector? comp)
      (mapv get-screen-component comp)
      (get-screen-component comp)))))

;; OVERLAY (Popover and bottom sheets)
(def dissmiss-overlay navigation/dissmiss-overlay)

(defn show-overlay
  ([comp] (show-overlay comp {}))
  ([comp opts]
   (dissmiss-overlay comp)
   (navigation/show-overlay
    {:component {:name    comp
                 :id      comp
                 :options (merge (cond-> (roots/status-bar-options)
                                   (and platform/android? (not (colors/dark?)))
                                   (assoc-in [:statusBar :backgroundColor] "#99999A"))
                                 {:layout  {:componentBackgroundColor (if platform/android?
                                                                        colors/neutral-80-opa-20 ;; TODO
                                                                                                 ;; adjust
                                                                                                 ;; color
                                                                        "transparent")}
                                  :overlay {:interceptTouchOutside true}}
                                 opts)}})))

;; POPOVER
(re-frame/reg-fx :show-popover (fn [] (show-overlay "popover")))
(re-frame/reg-fx :hide-popover (fn [] (dissmiss-overlay "popover")))

;; TOAST
(re-frame/reg-fx :show-toasts
                 (fn []
                   (show-overlay "toasts"
                                 {:overlay {:interceptTouchOutside false}
                                  :layout  {:componentBackgroundColor :transparent}})))
(re-frame/reg-fx :hide-toasts (fn [] (dissmiss-overlay "toasts")))

;; VISIBILITY STATUS POPOVER
(re-frame/reg-fx :show-visibility-status-popover
                 (fn [] (show-overlay "visibility-status-popover")))
(re-frame/reg-fx :hide-visibility-status-popover
                 (fn [] (dissmiss-overlay "visibility-status-popover")))

;; BOTTOM SHEETS
(re-frame/reg-fx :show-bottom-sheet-overlay (fn [] (show-overlay "bottom-sheet")))
(re-frame/reg-fx :dismiss-bottom-sheet-overlay (fn [] (dissmiss-overlay "bottom-sheet")))

;; WALLET CONNECT
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

;; SIGNING
(re-frame/reg-fx :show-signing-sheet (fn [] (show-overlay "signing-sheet")))
(re-frame/reg-fx :hide-signing-sheet (fn [] (dissmiss-overlay "signing-sheet")))

;; Select account
(re-frame/reg-fx :show-select-acc-sheet (fn [] (show-overlay "select-acc-sheet")))
(re-frame/reg-fx :hide-select-acc-sheet (fn [] (dissmiss-overlay "select-acc-sheet")))

(defonce
  _
  [(navigation/set-default-options {:layout {:orientation "portrait"}})
   (navigation/set-lazy-component-registrator reg-comp)
   (navigation/reg-button-pressed-listener button-pressed-listener)
   (navigation/reg-modal-dismissed-listener modal-dismissed-listener)
   (navigation/reg-component-did-appear-listener component-did-appear-listener)
   (navigation/reg-component-did-disappear-listener component-did-disappear-listener)
   (navigation/reg-app-launched-listener app-launched-listener)

   (navigation/register-component
    "popover"
    (fn [] (gesture/gesture-handler-root-hoc views/popover-comp))
    (fn [] views/popover-comp))

   (navigation/register-component
    "toasts"
    (fn [] views/toasts-comp)
    js/undefined)

   (navigation/register-component
    "visibility-status-popover"
    (fn [] (gesture/gesture-handler-root-hoc views/visibility-status-popover-comp))
    (fn [] views/visibility-status-popover-comp))

   (navigation/register-component
    "bottom-sheet"
    (fn [] (gesture/gesture-handler-root-hoc views/sheet-comp))
    (fn [] views/sheet-comp))

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

;; NAVIGATION

(re-frame/reg-fx
 :navigate-to-fx
 (fn [key]
   (log/debug :navigate-to-fx key)
   (navigate key)))

(re-frame/reg-fx
 :navigate-back-fx
 (fn []
   (log/debug :navigate-back-fx)
   (if @state/curr-modal
     (dissmissModal)
     (navigation/pop (name @state/root-comp-id)))))

(re-frame/reg-fx
 :navigate-replace-fx
 (fn [view-id]
   (log/debug :navigate-replace-fx view-id)
   (navigation/pop (name @state/root-comp-id))
   (navigate view-id)))

;; NAVIGATION 2

(re-frame/reg-fx
 :change-root-status-bar-style-fx
 (fn [style]
   (navigation/merge-options "shell-stack" {:statusBar {:style style}})))

(re-frame/reg-fx
 :change-root-nav-bar-color-fx
 (fn [color]
   (navigation/merge-options "shell-stack" {:navigationBar {:backgroundColor color}})))

(re-frame/reg-fx
 :pop-to-root-tab-fx
 (fn [tab]
   (navigation/pop-to-root tab)))

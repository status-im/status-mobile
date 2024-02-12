(ns status-im.navigation.effects
  (:require
   [quo.theme :as quo.theme]
   [react-native.core :as rn]
   [react-native.navigation :as navigation]
   [status-im.navigation.options :as options]
   [status-im.navigation.roots :as roots]
   [status-im.navigation.state :as state]
   [status-im.navigation.view :as views]
   [taoensso.timbre :as log]
   [utils.re-frame :as rf]))

(def previous-screen (atom nil))

(defn- set-status-bar-color
  [theme view-id]  
  (when-not (= @previous-screen :settings-syncing)
    (rn/set-status-bar-style
     (if (or (= theme :dark)
             (quo.theme/dark?))
       "light-content"
       "dark-content")
     true))
  (reset! previous-screen view-id))

(rf/reg-fx :set-view-id-fx
 (fn [view-id]
   (let [screen-theme (get-in (get views/screens view-id) [:options :theme])]
     (set-status-bar-color screen-theme view-id)
     (rf/dispatch [:screens/on-will-focus view-id])
     (when-let [{:keys [on-focus]} (get views/screens view-id)]
       (when on-focus
         (rf/dispatch on-focus))))))

(defn set-view-id
  [view-id]
  (when (get views/screens view-id)
    (rf/dispatch [:set-view-id view-id])))

(defn- dismiss-all-modals
  []
  (when @state/curr-modal
    (reset! state/curr-modal false)
    (reset! state/dissmissing true)
    (doseq [modal @state/modals]
      (navigation/dismiss-modal (name modal)))
    (reset! state/modals [])))

;;;; Root

(rf/reg-fx :set-root
 (fn [root-id]
   (let [root (get (roots/roots) root-id)]
     (dismiss-all-modals)
     (rf/dispatch [:profile.settings/switch-theme
                   (get roots/themes root-id)
                   root-id])
     (reset! state/root-id (or (get-in root [:root :stack :id]) root-id))
     (navigation/set-root root))))

;;;; Navigate to

(defn- navigate
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

(rf/reg-fx :navigate-to navigate)

;;;; Navigate to within stack

(defn- navigate-to-within-stack
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

(rf/reg-fx :navigate-to-within-stack navigate-to-within-stack)

(rf/reg-fx :navigate-replace-fx
 (fn [view-id]
   (navigation/pop (name @state/root-id))
   (navigate view-id)))

(defn dismiss-modal
  ([] (dismiss-modal nil))
  ([comp-id]
   (reset! state/dissmissing true)
   (navigation/dismiss-modal (name (or comp-id (last @state/modals))))))

(rf/reg-fx :navigate-back
 (fn []
   (if @state/curr-modal
     (dismiss-modal)
     (navigation/pop (name @state/root-id)))))

(rf/reg-fx :navigate-back-within-stack
 (fn [comp-id]
   (navigation/pop (name comp-id))))

(rf/reg-fx :navigate-back-to
 (fn [comp-id]
   (navigation/pop-to (name comp-id))))

(rf/reg-fx :dismiss-modal
 (fn [comp-id]
   (dismiss-modal (name comp-id))))

(defn- pop-to-root
  [root-id]
  (navigation/pop-to-root root-id)
  (dismiss-all-modals))

(rf/reg-fx :pop-to-root-fx pop-to-root)

;;;; Modal

(defn open-modal
  [component]
  (let [{:keys [options name]} (get views/screens component)
        sheet?                 (:sheet? options)
        screen-theme           (:theme options)]
    (set-status-bar-color screen-theme name)
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

(rf/reg-fx :open-modal-fx open-modal)

;;;; Overlay

(defn show-overlay
  ([component] (show-overlay component {}))
  ([component opts]
   (navigation/dissmiss-overlay component)
   (navigation/show-overlay
    {:component {:name    component
                 :id      component
                 :options (merge (options/statusbar)
                                 {:layout  {:componentBackgroundColor :transparent
                                            :orientation              ["portrait"]}
                                  :overlay {:interceptTouchOutside true}}
                                 opts)}})))

(rf/reg-fx :show-toasts
 (fn []
   (show-overlay "toasts"
                 {:overlay {:interceptTouchOutside false}
                  :layout  {:componentBackgroundColor :transparent
                            :orientation              ["portrait"]}})))

(rf/reg-fx :hide-toasts
 (fn [] (navigation/dissmiss-overlay "toasts")))

;;;; Bottom sheet

(rf/reg-fx :show-bottom-sheet
 (fn [] (show-overlay "bottom-sheet")))

(rf/reg-fx :hide-bottom-sheet
 (fn [] (navigation/dissmiss-overlay "bottom-sheet")))

;;;; Merge options

(rf/reg-fx :merge-options
 (fn [{:keys [id options]}]
   (navigation/merge-options id options)))

;;;; Legacy (should be removed in status 2.0)

(defn- get-screen-component
  [component]
  (let [{:keys [options]} (get views/screens component)]
    {:component {:id      component
                 :name    component
                 :options (merge (options/statusbar-and-navbar)
                                 options
                                 (options/merge-top-bar (options/topbar-options) options))}}))

(rf/reg-fx :set-stack-root-fx
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

(rf/reg-fx :show-popover
 (fn [] (show-overlay "popover")))

(rf/reg-fx :hide-popover
 (fn [] (navigation/dissmiss-overlay "popover")))

(rf/reg-fx :show-visibility-status-popover
 (fn [] (show-overlay "visibility-status-popover")))

(rf/reg-fx :hide-visibility-status-popover
 (fn [] (navigation/dissmiss-overlay "visibility-status-popover")))

(rf/reg-fx :show-bottom-sheet-overlay-old
 (fn [] (show-overlay "bottom-sheet-old")))

(rf/reg-fx :dismiss-bottom-sheet-overlay-old
 (fn [] (navigation/dissmiss-overlay "bottom-sheet-old")))

(rf/reg-fx :show-wallet-connect-sheet
 (fn [] (show-overlay "wallet-connect-sheet")))

(rf/reg-fx :hide-wallet-connect-sheet
 (fn [] (navigation/dissmiss-overlay "wallet-connect-sheet")))

(rf/reg-fx :show-wallet-connect-success-sheet
 (fn [] (show-overlay "wallet-connect-success-sheet")))

(rf/reg-fx :hide-wallet-connect-success-sheet
 (fn [] (navigation/dissmiss-overlay "wallet-connect-success-sheet")))

(rf/reg-fx :show-wallet-connect-app-management-sheet
 (fn [] (show-overlay "wallet-connect-app-management-sheet")))

(rf/reg-fx :hide-wallet-connect-app-management-sheet
 (fn [] (navigation/dissmiss-overlay "wallet-connect-app-management-sheet")))

(rf/reg-fx :show-signing-sheet
 (fn [] (show-overlay "signing-sheet")))

(rf/reg-fx :hide-signing-sheet
 (fn [] (navigation/dissmiss-overlay "signing-sheet")))

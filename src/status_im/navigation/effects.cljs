(ns status-im.navigation.effects
  (:require
    [quo.theme]
    [react-native.core :as rn]
    [react-native.navigation :as navigation]
    [status-im.navigation.options :as options]
    [status-im.navigation.roots :as roots]
    [status-im.navigation.state :as state]
    [status-im.navigation.view :as views]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn- set-status-bar-color
  [theme]
  (rn/set-status-bar-style
   (if (= theme :dark)
     "light-content"
     "dark-content")
   true))

(rf/reg-fx :set-view-id-fx
 (fn [view-id]
   (rf/dispatch [:screens/on-will-focus view-id])
   (when-let [{:keys [on-focus options]} (get views/screens view-id)]
     (set-status-bar-color (or (:theme options)
                               (quo.theme/get-theme)))
     (when on-focus
       (rf/dispatch on-focus)))))

(defn- dismiss-all-modals
  []
  (when (seq @state/modals)
    (reset! state/dissmissing true)
    (doseq [modal @state/modals]
      (navigation/dismiss-modal (name modal)))
    (state/navigation-pop-from (first @state/modals))
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
     (navigation/set-root root)
     (state/navigation-state-reset [{:id   root-id
                                     :type :root}]))))

;;;; Navigate to

(defn- navigate
  [component]
  (let [{:keys [options]} (get views/screens component)]
    (dismiss-all-modals)
    (navigation/push
     (name @state/root-id)
     {:component {:id      component
                  :name    component
                  :options (merge (options/root-options {:theme (:theme options)})
                                  options)}})
    (state/navigation-state-push {:id     component
                                  :type   :stack
                                  :parent @state/root-id})))

(rf/reg-fx :navigate-to navigate)

;;;; Navigate to within stack

(defn- navigate-to-within-stack
  [[component comp-id]]
  (let [{:keys [options]} (get views/screens component)]
    (navigation/push
     (name comp-id)
     {:component {:id      component
                  :name    component
                  :options (merge
                            (options/root-options {:theme (:theme options)})
                            options)}})
    (state/navigation-state-push {:id     component
                                  :type   :stack
                                  :parent comp-id})))

(rf/reg-fx :navigate-to-within-stack navigate-to-within-stack)

(defn dismiss-modal
  ([] (dismiss-modal nil))
  ([comp-id]
   (reset! state/dissmissing true)
   (navigation/dismiss-modal (name (or comp-id (last @state/modals))))
   (state/navigation-pop-from comp-id)))

(defn navigate-back
  []
  (when-let [{:keys [type parent id]} (last (state/get-navigation-state))]
    (cond
      (and (= type :modal) id)
      (dismiss-modal id)
      (and (= type :stack) parent)
      (do
        (navigation/pop (name parent))
        (state/navigation-state-pop)))))

(rf/reg-fx :navigate-back navigate-back)

(rf/reg-fx :navigate-replace-fx
 (fn [view-id]
   (navigate-back)
   (navigate view-id)))

(rf/reg-fx :navigate-back-to
 (fn [comp-id]
   (navigation/pop-to (name comp-id))
   (state/navigation-pop-after comp-id)))

(rf/reg-fx :dismiss-modal
 (fn [comp-id]
   (dismiss-modal comp-id)))

(defn- pop-to-root
  [root-id]
  (dismiss-all-modals)
  (navigation/pop-to-root root-id)
  (state/navigation-pop-after root-id))

(rf/reg-fx :pop-to-root-fx pop-to-root)

;;;; Modal

(defn open-modal
  [component]
  (let [{:keys [options]} (get views/screens component)
        sheet?            (:sheet? options)]
    (if @state/dissmissing
      (reset! state/dissmissing component)
      (do
        (swap! state/modals conj component)
        (navigation/show-modal
         {:stack {:children [{:component
                              {:name    component
                               :id      component
                               :options (merge (options/root-options {:theme (:theme options)})
                                               options
                                               (when sheet?
                                                 options/sheet-options))}}]}})))
    (state/navigation-state-push {:id   component
                                  :type :modal})))

(rf/reg-fx :open-modal-fx open-modal)

;;;; Overlay

(defn show-overlay
  ([component] (show-overlay component {}))
  ([component opts]
   (navigation/dissmiss-overlay component)
   (navigation/show-overlay
    {:component {:name    component
                 :id      component
                 :options (merge (options/statusbar-and-navbar-options (:theme opts) nil nil)
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

;;;; Alert Banner
(rf/reg-fx :show-alert-banner
 (fn [] (show-overlay "alert-banner" {:overlay {:interceptTouchOutside false}})))

(rf/reg-fx :hide-alert-banner
 (fn [] (navigation/dissmiss-overlay "alert-banner")))

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
                 :options (merge (options/statusbar-and-navbar-options (:theme options) nil nil)
                                 options)}}))

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

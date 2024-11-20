(ns status-im.navigation.effects
  (:require
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.navigation :as navigation]
    [react-native.platform :as platform]
    [react-native.share :as share]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.navigation.options :as options]
    [status-im.navigation.roots :as roots]
    [status-im.navigation.state :as state]
    [status-im.navigation.view :as views]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn get-status-nav-color
  [view-id theme]
  (let [theme            (or (get-in views/screens [view-id :options :theme])
                             theme)
        status-bar-theme (if (or (= theme :dark)
                                 @state/alert-banner-shown?)
                           :light
                           :dark)
        home-stack?      (some #(= view-id %) shell.constants/stacks-ids)
        ;; Home screen nav bar always dark due to bottom tabs
        nav-bar-color    (if (or home-stack?
                                 (= view-id :shell-stack)
                                 (= theme :dark))
                           colors/neutral-100
                           colors/white)
        comp-id          (if (some #(= view-id %) shell.constants/stacks-ids) :shell-stack view-id)]
    [status-bar-theme nav-bar-color comp-id]))

(defn reload-status-nav-color-fx
  [[view-id theme]]
  (when (and (= @state/root-id :shell-stack) view-id)
    (let [[status-bar-theme nav-bar-color comp-id] (get-status-nav-color view-id theme)]
      (when platform/android?
        (navigation/merge-options
         (name comp-id)
         {:statusBar     {:style status-bar-theme}
          :navigationBar {:backgroundColor nav-bar-color}})))))

(rf/reg-fx :reload-status-nav-color-fx reload-status-nav-color-fx)

(rf/reg-fx :set-view-id-fx
 (fn [[view-id theme]]
   (reload-status-nav-color-fx [view-id theme])
   (rf/dispatch [:screens/on-will-focus view-id])
   (when-let [{:keys [on-focus]} (get views/screens view-id)]
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
 (fn [[root-id theme]]
   (let [[status-bar-theme] (get-status-nav-color root-id theme)
         root               (get (roots/roots status-bar-theme) root-id)]
     (dismiss-all-modals)
     (reset! state/root-id (or (get-in root [:root :stack :id]) root-id))
     (navigation/set-root root)
     (state/navigation-state-reset [{:id   root-id
                                     :type :root}]))))

;;;; Navigate to

(defn- navigate
  [[component theme animations]]
  (let [{:keys [options]} (get views/screens component)
        options           (if (map? animations) (assoc options :animations animations) options)]
    (dismiss-all-modals)
    (navigation/push
     (name @state/root-id)
     {:component {:id      component
                  :name    component
                  :options (merge (options/root-options {:theme (or (:theme options) theme)})
                                  options)}})
    (state/navigation-state-push {:id     component
                                  :type   :stack
                                  :parent @state/root-id})))

(rf/reg-fx :navigate-to navigate)

;;;; Navigate to within stack

(defn- navigate-to-within-stack
  [[component comp-id theme]]
  (let [{:keys [options]} (get views/screens component)]
    (navigation/push
     (name comp-id)
     {:component {:id      component
                  :name    component
                  :options (merge
                            (options/root-options {:theme theme})
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
  [[component theme]]
  (let [{:keys [options]} (get views/screens component)
        sheet?            (:sheet? options)]
    (if @state/dissmissing
      (reset! state/dissmissing [component theme])
      (do
        (swap! state/modals conj component)
        (navigation/show-modal
         {:stack {:children [{:component
                              {:name    component
                               :id      component
                               :options (merge (options/root-options {:theme (or (:theme options)
                                                                                 theme)})
                                               options
                                               (when sheet?
                                                 options/sheet-options))}}]}})))
    (state/navigation-state-push {:id   component
                                  :type :modal})))

(rf/reg-fx :open-modal-fx open-modal)

;;;; Share

(rf/reg-fx :effects.share/open
 (fn [{:keys [options on-success on-error]}]
   (cond-> (share/open options)
     (fn? on-success) (.then on-success)
     :always          (.catch (fn [error]
                                (log/error "Failed to share content"
                                           {:error  error
                                            :effect :effects.share/open})
                                (when (fn? on-error)
                                  (on-error error)))))))

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
                                  :overlay {:interceptTouchOutside true
                                            :handleKeyboardEvents  true}}
                                 opts)}})))

(rf/reg-fx :show-toasts
 (fn [[view-id theme]]
   (let [[status-bar-theme nav-bar-color] (get-status-nav-color view-id theme)]
     (show-overlay "toasts"
                   (assoc (options/statusbar-and-navbar-options nil status-bar-theme nav-bar-color)
                          :overlay
                          {:interceptTouchOutside false})))))

(rf/reg-fx :hide-toasts
 (fn [] (navigation/dissmiss-overlay "toasts")))

;;;; Bottom sheet

(rf/reg-fx :show-bottom-sheet
 (fn [options]
   (show-overlay "bottom-sheet" options)))

(rf/reg-fx :hide-bottom-sheet
 (fn [] (navigation/dissmiss-overlay "bottom-sheet")))

;;;; Alert Banner

(rf/reg-fx :show-alert-banner
 (fn [[view-id theme]]
   (show-overlay "alert-banner"
                 (assoc (options/statusbar-and-navbar-options nil :light nil)
                        :overlay
                        {:interceptTouchOutside false}))
   (reset! state/alert-banner-shown? true)
   (reload-status-nav-color-fx [view-id theme])))

(rf/reg-fx :hide-alert-banner
 (fn [[view-id theme]]
   (navigation/dissmiss-overlay "alert-banner")
   (reset! state/alert-banner-shown? false)
   (reload-status-nav-color-fx [view-id theme])))

;;;; NFC sheet

(rf/reg-fx :show-nfc-sheet
 (fn [] (show-overlay "nfc-sheet")))

(rf/reg-fx :hide-nfc-sheet
 (fn [] (navigation/dissmiss-overlay "nfc-sheet")))

;;;; Legacy (should be removed in status 2.0)
(rf/reg-fx :show-popover
 (fn [] (show-overlay "popover")))

(rf/reg-fx :hide-popover
 (fn [] (navigation/dissmiss-overlay "popover")))

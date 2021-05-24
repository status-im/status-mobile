(ns status-im.navigation.core
  (:require
   [status-im.ui.components.colors :as colors]
   [re-frame.core :as re-frame]
   [status-im.ui.screens.views :as views]
   [status-im.utils.platform :as platform]
   [status-im.navigation.roots :as roots]
   ["react-native-navigation" :refer (Navigation)]
   ["react-native-gesture-handler" :refer (gestureHandlerRootHOC)]
   [status-im.ui.components.react :as react]
   [quo.components.text-input :as quo.text-input]
   [status-im.ui.components.icons.icons :as icons]
   [quo.design-system.colors :as quo.colors]
   ["react-native" :as rn]))

(def debug? ^boolean js/goog.DEBUG)

(def splash-screen (-> rn .-NativeModules .-SplashScreen))

(defonce root-comp-id (atom nil))
(defonce root-id (atom nil))
(defonce pushed-screen-id (atom nil))
(defonce curr-modal (atom nil))
(defonce modals (atom []))
(defonce dissmissing (atom false))

;; REGISTER COMPONENT (LAZY)
(defn reg-comp [key]
  (if-let [comp (get views/components (keyword key))]
    (.registerComponent Navigation key (fn [] (views/component comp)))
    (let [screen (views/screen key)]
      (.registerComponent Navigation key (fn [] (gestureHandlerRootHOC screen)) (fn [] screen)))))

(defonce rset-lazy-reg
  (.setLazyComponentRegistrator Navigation reg-comp))

;; PUSH SCREEN
(defn navigate [comp]
  (let [{:keys [options]} (get views/screens comp)]
    (.push Navigation
           (name @root-comp-id)
           (clj->js {:component {:id      comp
                                 :name    comp
                                 :options (merge options
                                                 (roots/status-bar-options)
                                                 (roots/merge-top-bar (roots/topbar-options) options))}}))
    ;;if we push the screen from modal, we want to dismiss all modals
    (when @curr-modal
      (reset! curr-modal false)
      (reset! modals [])
      (.dismissAllModals Navigation))))

;; OPEN MODAL
(defn update-modal-topbar-options [options]
  (merge options
         (roots/merge-top-bar {:elevation       0
                               :noBorder        true
                               :title           {:color colors/black}
                               :background      {:color colors/white}
                               :leftButtonColor colors/black
                               :leftButtons     {:id   "dismiss-modal"
                                                 :icon (icons/icon-source :main-icons/close)}}
                              options)))

(defn open-modal [comp]
  (let [{:keys [options]} (get views/screens comp)]
    (if @dissmissing
      (reset! dissmissing comp)
      (do
        (println "SHOW MODAL" comp)
        (reset! curr-modal true)
        (swap! modals conj comp)
        (.showModal Navigation
                    (clj->js {:stack {:children
                                      [{:component
                                        {:name    comp
                                         :id      comp
                                         :options (update-modal-topbar-options
                                                   (merge (roots/status-bar-options)
                                                          (roots/default-root)
                                                          options))}}]}}))))))

(re-frame/reg-fx :open-modal-fx open-modal)

;; DISSMISS MODAL
(defn dissmissModal []
  (println "dissmissModal" @modals)
  (reset! dissmissing true)
  (.dismissModal Navigation (name (last @modals))))

(defonce register-nav-button-reg
  (.registerNavigationButtonPressedListener
   (.events Navigation)
   (fn [^js evn]
     (let [id (.-buttonId evn)]
       (if (= "dismiss-modal" id)
         (do
           (when-let [event (get-in views/screens [(last @modals) :on-dissmiss])]
             (re-frame/dispatch event))
           (dissmissModal))
         (when-let [handler (get-in views/screens [(keyword id) :right-handler])]
           (handler)))))))

(defonce register-modal-reg
  (.registerModalDismissedListener
   (.events Navigation)
   (fn [_]
     (println "DismissedListener" @dissmissing @modals)
     (if (> (count @modals) 1)
       (let [new-modals (butlast @modals)]
         (reset! modals (vec new-modals))
         (re-frame/dispatch [:set :view-id (last new-modals)]))
       (do
         (reset! modals [])
         (reset! curr-modal false)
         (re-frame/dispatch [:set :view-id @pushed-screen-id])))

     (let [comp @dissmissing]
       (reset! dissmissing false)
       (when (keyword? comp)
         (open-modal comp))))))

;; SCREEN DID APPEAR
(defonce screen-appear-reg
  (.registerComponentDidAppearListener
   (.events Navigation)
   (fn [^js evn]
     (let [view-id (keyword (.-componentName evn))]
       (when-let [{:keys [on-focus]} (get views/screens view-id)]
         (when (and (not= view-id :bottom-sheet) (not= view-id :popover))
           (re-frame/dispatch [:set :view-id view-id])
           (when on-focus
             (re-frame/dispatch on-focus))
           (when-not @curr-modal
             (reset! pushed-screen-id view-id))))))))

;; SCREEN DID DISAPPEAR
(defonce screen-disappear-reg
  (.registerComponentDidDisappearListener
   (.events Navigation)
   (fn [_]
     (doseq [[_ {:keys [ref value]}] @quo.text-input/text-input-refs]
       (.setNativeProps ^js ref (clj->js {:text value})))
     (doseq [[^js text-input default-value] @react/text-input-refs]
       (.setNativeProps text-input (clj->js {:text default-value}))))))

;; SET ROOT
(re-frame/reg-fx
 :init-root-fx
 (fn [new-root-id]
   (reset! root-comp-id new-root-id)
   (reset! root-id @root-comp-id)
   (.setRoot Navigation (clj->js (get (roots/roots) new-root-id)))))

(re-frame/reg-fx
 :init-root-with-component-fx
 (fn [[new-root-id new-root-comp-id]]
   (reset! root-comp-id new-root-comp-id)
   (reset! root-id @root-comp-id)
   (.setRoot Navigation (clj->js (get (roots/roots) new-root-id)))))

(defonce rset-app-launched
  (.registerAppLaunchedListener (.events Navigation)
                                (fn []
                                  (when @root-id
                                    (reset! root-comp-id @root-id)
                                    (.setRoot Navigation (clj->js (get (roots/roots) @root-id))))
                                  (.hide ^js splash-screen))))

(defn get-screen-component [comp]
  (let [{:keys [options]} (get views/screens comp)]
    {:component {:id      comp
                 :name    comp
                 :options (merge options
                                 (roots/status-bar-options)
                                 (roots/merge-top-bar (roots/topbar-options) options))}}))

;; SET STACK ROOT
(re-frame/reg-fx
 :set-stack-root-fx
 (fn [[stack comp]]
   (.setStackRoot Navigation
                  (name stack)
                  (clj->js (if (vector? comp)
                             (mapv get-screen-component comp)
                             (get-screen-component comp))))))

;; BOTTOM TABS
(def tab-root-ids {0 :chat-stack
                   1 :browser-stack
                   2 :wallet-stack
                   3 :status-stack
                   4 :profile-stack})

(def tab-key-idx {:chat    0
                  :browser 1
                  :wallet  2
                  :status  3
                  :profile 4})

(re-frame/reg-fx
 :rnn-change-tab-fx
 (fn [tab]
   (reset! root-comp-id (get tab-root-ids (get tab-key-idx tab)))
   (.mergeOptions Navigation "tabs-stack" (clj->js {:bottomTabs {:currentTabIndex (get tab-key-idx tab)}}))))

;issue on ios https://github.com/wix/react-native-navigation/issues/7146
(re-frame/reg-fx
 :rnn-change-tab-count-fx
 (fn [[tab cnt]]
   (.mergeOptions Navigation
                  (name (get tab-root-ids (get tab-key-idx tab)))
                  (clj->js {:bottomTab (cond
                                         (or (pos? cnt) (pos? (:other cnt)))
                                         (if (and (= :chat tab) platform/ios?)
                                           {:dotIndicator {:visible true}}
                                           {:badge (str (or (:other cnt) cnt)) :dotIndicator {:visible false}})
                                         (pos? (:public cnt))
                                         (if platform/ios?
                                           {:dotIndicator {:visible true}}
                                           {:badge nil :dotIndicator {:visible true}})
                                         :else
                                         (if (and (= :chat tab) platform/ios?)
                                           {:dotIndicator {:visible false}}
                                           {:dotIndicator {:visible false} :badge nil}))}))))

(re-frame/reg-fx
 :rnn-pop-to-root-tab-fx
 (fn [comp]
   (.popToRoot Navigation (name comp))))

(defonce register-bottom-tab-reg
  (.registerBottomTabSelectedListener
   (.events Navigation)
   (fn [^js evn]
     (let [comp (get tab-root-ids (.-selectedTabIndex evn))]
       (when (and platform/android? (= @root-comp-id comp))
         (.popToRoot Navigation (name comp)))
       (reset! root-comp-id comp)))))

;; OVERLAY (Popover and bottom sheets)
(defn dissmiss-overlay [comp]
  (.dismissOverlay Navigation comp))

(defn show-overlay [comp]
  (dissmiss-overlay comp)
  (.showOverlay Navigation
                (clj->js
                 {:component {:name    comp
                              :id      comp
                              :options (merge (cond-> (roots/status-bar-options)
                                                (and platform/android? (not (colors/dark?)))
                                                (assoc-in [:statusBar :translucent] true))
                                              {:layout  {:componentBackgroundColor (if platform/android?
                                                                                     (:backdrop @quo.colors/theme)
                                                                                     "transparent")}
                                               :overlay {:interceptTouchOutside true}})}})))

;; POPOVER
(defonce popover-reg
  (.registerComponent Navigation
                      "popover"
                      (fn [] (gestureHandlerRootHOC views/popover-comp))
                      (fn [] views/popover-comp)))

(re-frame/reg-fx :rnn-show-popover (fn [] (show-overlay "popover")))
(re-frame/reg-fx :rnn-hide-popover (fn [] (dissmiss-overlay "popover")))

;; BOTTOM SHEETS
(defonce bottom-sheet-reg
  (.registerComponent Navigation
                      "bottom-sheet"
                      (fn [] (gestureHandlerRootHOC views/sheet-comp))
                      (fn [] views/sheet-comp)))

(re-frame/reg-fx :rnn-show-bottom-sheet (fn [] (show-overlay "bottom-sheet")))
(re-frame/reg-fx :rnn-hide-bottom-sheet (fn [] (dissmiss-overlay "bottom-sheet")))

;; SIGNING

(defonce signing-sheet-reg
  (.registerComponent Navigation
                      "signing-sheet"
                      (fn [] (gestureHandlerRootHOC views/signing-comp))
                      (fn [] views/signing-comp)))

(re-frame/reg-fx :rnn-show-signing-sheet (fn [] (show-overlay "signing-sheet")))
(re-frame/reg-fx :rnn-hide-signing-sheet (fn [] (dissmiss-overlay "signing-sheet")))

;; Select account
;; TODO why is this not a regular bottom sheet ?

(defonce select-acc-sheet-reg
  (.registerComponent Navigation
                      "select-acc-sheet"
                      (fn [] (gestureHandlerRootHOC views/select-acc-comp))
                      (fn [] views/select-acc-comp)))

(re-frame/reg-fx :rnn-show-select-acc-sheet (fn [] (show-overlay "select-acc-sheet")))
(re-frame/reg-fx :rnn-hide-select-acc-sheet (fn [] (dissmiss-overlay "select-acc-sheet")))

;; NAVIGATION

(re-frame/reg-fx
 :rnn-navigate-to-fx
 (fn [key]
   ;;TODO WHY #{:home} ? we need to review all navigations to root screens home, wallet profile etc
   (when-not (#{:home} key)
     (navigate key))))

(re-frame/reg-fx
 :rnn-navigate-back-fx
 (fn []
   (if @curr-modal
     (dissmissModal)
     (.pop Navigation (name @root-comp-id)))))

(re-frame/reg-fx
 :navigate-replace-fx
 (fn [view-id]
   (.pop Navigation (name @root-comp-id))
   (navigate view-id)))

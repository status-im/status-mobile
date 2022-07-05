(ns status-im.navigation.core
  (:require
   ["react-native" :as rn]
   [clojure.set :as clojure.set]
   ["react-native-gesture-handler" :refer (gestureHandlerRootHOC)]
   ["react-native-navigation" :refer (Navigation)]
   [quo.components.text-input :as quo.text-input]
   [quo.design-system.colors :as quo.colors]
   [re-frame.core :as re-frame]
   [status-im.navigation.roots :as roots]
   [status-im.ui.components.icons.icons :as icons]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.views :as views]
   [status-im.utils.fx :as fx]
   [status-im.utils.platform :as platform]
   [taoensso.timbre :as log]
   [status-im.multiaccounts.login.core :as login-core]
   [status-im.navigation.state :as state]))

(def debug? ^boolean js/goog.DEBUG)

(def splash-screen (-> rn .-NativeModules .-SplashScreen))

(defonce set-navigation-default-options
  (.setDefaultOptions Navigation (clj->js {:layout {:orientation "portrait"}})))

;; REGISTER COMPONENT (LAZY)
(defn reg-comp [key]
  (log/debug "reg-comp" key)
  (if-let [comp (get views/components (keyword key))]
    (.registerComponent Navigation key (fn [] (views/component comp)))
    (let [screen (views/screen key)]
      (.registerComponent Navigation key (fn [] (gestureHandlerRootHOC screen)) (fn [] screen)))))

(defonce rset-lazy-reg
  (.setLazyComponentRegistrator Navigation reg-comp))

(defn dismiss-all-modals []
  (log/debug "dissmiss-all-modals")
  (when @state/curr-modal
    (reset! state/curr-modal false)
    (reset! state/dissmissing true)
    (doseq [modal @state/modals]
      (.dismissModal Navigation (name modal)))
    (reset! state/modals [])))

;; PUSH SCREEN
(defn navigate [comp]
  (log/debug "NAVIGATE" comp)
  (let [{:keys [options]} (get views/screens comp)]
    (.push Navigation
           (name @state/root-comp-id)
           (clj->js {:component {:id      comp
                                 :name    comp
                                 :options (merge options
                                                 (roots/status-bar-options)
                                                 (roots/merge-top-bar (roots/topbar-options) options))}}))
    ;;if we push the screen from modal, we want to dismiss all modals
    (dismiss-all-modals)))

;; OPEN MODAL
(defn update-modal-topbar-options [options]
  (log/debug "update-modal-topbar-options" options)
  (merge options
         (roots/merge-top-bar {:elevation       0
                               :noBorder        true
                               :title           {:color quo.colors/black}
                               :background      {:color quo.colors/white}
                               :leftButtonColor quo.colors/black
                               :leftButtons     {:id   "dismiss-modal"
                                                 :icon (icons/icon-source :main-icons/close)}}
                              options)))

(defn open-modal [comp]
  (log/debug "open-modal" comp)
  (let [{:keys [options]} (get views/screens comp)]
    (if @state/dissmissing
      (reset! state/dissmissing comp)
      (do
        (reset! state/curr-modal true)
        (swap! state/modals conj comp)
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
  (log/debug "dissmissModal")
  (reset! state/dissmissing true)
  (.dismissModal Navigation (name (last @state/modals))))

(defonce register-nav-button-reg
  (.registerNavigationButtonPressedListener
   (.events Navigation)
   (fn [^js evn]
     (let [id (.-buttonId evn)]
       (if (= "dismiss-modal" id)
         (do
           (when-let [event (get-in views/screens [(last @state/modals) :on-dissmiss])]
             (re-frame/dispatch event))
           (dissmissModal))
         (when-let [handler (get-in views/screens [(keyword id) :right-handler])]
           (handler)))))))

(defn set-view-id [view-id]
  (log/debug "set-view-id" view-id)
  (when-let [{:keys [on-focus]} (get views/screens view-id)]
    (re-frame/dispatch [:set :view-id view-id])
    (re-frame/dispatch [:screens/on-will-focus view-id])
    (when on-focus
      (re-frame/dispatch on-focus))))

(defonce register-modal-reg
  (.registerModalDismissedListener
   (.events Navigation)
   (fn [_]
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
         (open-modal comp))))))

;; SCREEN DID APPEAR
(defonce screen-appear-reg
  (.registerComponentDidAppearListener
   (.events Navigation)
   (fn [^js evn]
     (let [view-id (keyword (.-componentName evn))]
       (log/debug "screen-appear-reg" view-id)
       (when (get views/screens view-id)
         (when (and (not= view-id :bottom-sheet)
                    (not= view-id :popover)
                    (not= view-id :visibility-status-popover))
           (set-view-id view-id)
           (when-not @state/curr-modal
             (reset! state/pushed-screen-id view-id))))))))

;; SCREEN DID DISAPPEAR
(defonce screen-disappear-reg
  (.registerComponentDidDisappearListener
   (.events Navigation)
   (fn [^js evn]
     (let [view-id (keyword (.-componentName evn))]
       (when-not (#{"popover" "bottom-sheet" "signing-sheet" "visibility-status-popover" "wallet-connect-sheet" "wallet-connect-success-sheet" "wallet-connect-app-management-sheet"}
                  (.-componentName evn))
         (re-frame/dispatch [::view-disappeared view-id])
         (doseq [[_ {:keys [ref value]}] @quo.text-input/text-input-refs]
           (.setNativeProps ^js ref (clj->js {:text value})))
         (doseq [[^js text-input default-value] @react/text-input-refs]
           (.setNativeProps text-input (clj->js {:text default-value}))))))))

;; SET ROOT
(re-frame/reg-fx
 :init-root-fx
 (fn [new-root-id]
   (log/debug :init-root-fx new-root-id)
   (reset! state/root-comp-id new-root-id)
   (reset! state/root-id @state/root-comp-id)
   (.setRoot Navigation (clj->js (get (roots/roots) new-root-id)))))

(re-frame/reg-fx
 :init-root-with-component-fx
 (fn [[new-root-id new-root-comp-id]]
   (log/debug :init-root-with-component-fx new-root-id new-root-comp-id)
   (reset! state/root-comp-id new-root-comp-id)
   (reset! state/root-id @state/root-comp-id)
   (.setRoot Navigation (clj->js (get (roots/roots) new-root-id)))))

(fx/defn set-multiaccount-root
  {:events [::set-multiaccount-root]}
  [{:keys [db]}]
  (log/debug :set-multiaccounts-root)
  (let [key-uid          (get-in db [:multiaccounts/login :key-uid])
        keycard-account? (boolean (get-in db [:multiaccounts/multiaccounts
                                              key-uid
                                              :keycard-pairing]))]
    {:init-root-fx (if keycard-account? :multiaccounts-keycard :multiaccounts)}))

(defonce rset-app-launched
  (.registerAppLaunchedListener (.events Navigation)
                                (fn []
                                  (reset! state/curr-modal false)
                                  (reset! state/dissmissing false)
                                  (if (or (= @state/root-id :multiaccounts)
                                          (= @state/root-id :multiaccounts-keycard))
                                    (re-frame/dispatch-sync [::set-multiaccount-root])
                                    (when @state/root-id
                                      (reset! state/root-comp-id @state/root-id)
                                      (.setRoot Navigation (clj->js (get (roots/roots) @state/root-id)))
                                      (re-frame/dispatch [::login-core/check-last-chat])))
                                  (.hide ^js splash-screen))))

(defn get-screen-component [comp]
  (log/debug :get-screen-component comp)
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
   (log/debug :set-stack-root-fx stack comp)
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
 :change-tab-fx
 (fn [tab]
   (log/debug :change-tab-fx)
   (reset! state/root-comp-id (get tab-root-ids (get tab-key-idx tab)))
   (.mergeOptions Navigation "tabs-stack" (clj->js {:bottomTabs {:currentTabIndex (get tab-key-idx tab)}}))
   ;;when we change tab we want to dismiss all modals
   (dismiss-all-modals)))

;issue on ios https://github.com/wix/react-native-navigation/issues/7146
(re-frame/reg-fx
 :change-tab-count-fx
 (fn [[tab cnt]]
   (log/debug :change-tab-count-fx tab cnt)
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
                                           {:dotIndicator {:visible false} :badge ""}))}))))

(re-frame/reg-fx
 :pop-to-root-tab-fx
 (fn [comp]
   (log/debug :pop-to-root-tab-fx comp)
   (dismiss-all-modals)
   (.popToRoot Navigation (name comp))))

(defonce register-bottom-tab-reg
  (.registerBottomTabSelectedListener
   (.events Navigation)
   (fn [^js evn]
     (let [selected-tab-index (.-selectedTabIndex evn)
           comp               (get tab-root-ids selected-tab-index)
           tab-key            (get (clojure.set/map-invert tab-key-idx) selected-tab-index)]
       (re-frame/dispatch [:set :current-tab tab-key])
       (when (= @state/root-comp-id comp)
         (when (= :chat tab-key)
           (re-frame/dispatch [:close-chat]))
         (when platform/android?
           (.popToRoot Navigation (name comp))))
       (reset! state/root-comp-id comp)))))

;; OVERLAY (Popover and bottom sheets)
(defn dissmiss-overlay [comp]
  (.catch (.dismissOverlay Navigation comp) #()))

(defn show-overlay [comp]
  (dissmiss-overlay comp)
  (.showOverlay Navigation
                (clj->js
                 {:component {:name    comp
                              :id      comp
                              :options (merge (cond-> (roots/status-bar-options)
                                                (and platform/android? (not (quo.colors/dark?)))
                                                (assoc-in [:statusBar :backgroundColor] "#99999A"))
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

(re-frame/reg-fx :show-popover (fn [] (show-overlay "popover")))
(re-frame/reg-fx :hide-popover (fn [] (dissmiss-overlay "popover")))

;; VISIBILITY STATUS POPOVER
(defonce visibility-status-popover-reg
  (.registerComponent Navigation
                      "visibility-status-popover"
                      (fn [] (gestureHandlerRootHOC views/visibility-status-popover-comp))
                      (fn [] views/visibility-status-popover-comp)))

(re-frame/reg-fx :show-visibility-status-popover
                 (fn [] (show-overlay "visibility-status-popover")))
(re-frame/reg-fx :hide-visibility-status-popover
                 (fn [] (dissmiss-overlay "visibility-status-popover")))

;; BOTTOM SHEETS
(defonce bottom-sheet-reg
  (.registerComponent Navigation
                      "bottom-sheet"
                      (fn [] (gestureHandlerRootHOC views/sheet-comp))
                      (fn [] views/sheet-comp)))

(defonce bottom-sheet-redesign-reg
  (.registerComponent Navigation
                      "bottom-sheet-redesign"
                      (fn [] (gestureHandlerRootHOC views/sheet-comp-redesign))
                      (fn [] views/sheet-comp-redesign)))

(re-frame/reg-fx :show-bottom-sheet (fn [] (show-overlay "bottom-sheet")))
(re-frame/reg-fx :hide-bottom-sheet (fn [] (dissmiss-overlay "bottom-sheet")))

(re-frame/reg-fx :show-bottom-sheet-redesign (fn [] (show-overlay "bottom-sheet-redesign")))
(re-frame/reg-fx :hide-bottom-sheet-redesign (fn [] (dissmiss-overlay "bottom-sheet-redesign")))

;; WALLET CONNECT

(defonce wallet-connect-sheet-reg
  (.registerComponent Navigation
                      "wallet-connect-sheet"
                      (fn [] (gestureHandlerRootHOC views/wallet-connect-comp))
                      (fn [] views/wallet-connect-comp)))

(defonce wallet-connect-success-sheet-reg
  (.registerComponent Navigation
                      "wallet-connect-success-sheet"
                      (fn [] (gestureHandlerRootHOC views/wallet-connect-success-comp))
                      (fn [] views/wallet-connect-success-comp)))

(defonce wallet-connect-app-management-sheet-reg
  (.registerComponent Navigation
                      "wallet-connect-app-management-sheet"
                      (fn [] (gestureHandlerRootHOC views/wallet-connect-app-management-comp))
                      (fn [] views/wallet-connect-app-management-comp)))

(re-frame/reg-fx :show-wallet-connect-sheet (fn [] (show-overlay "wallet-connect-sheet")))
(re-frame/reg-fx :hide-wallet-connect-sheet (fn [] (dissmiss-overlay "wallet-connect-sheet")))
(re-frame/reg-fx :show-wallet-connect-success-sheet (fn [] (show-overlay "wallet-connect-success-sheet")))
(re-frame/reg-fx :hide-wallet-connect-success-sheet (fn [] (dissmiss-overlay "wallet-connect-success-sheet")))
(re-frame/reg-fx :show-wallet-connect-app-management-sheet (fn [] (show-overlay "wallet-connect-app-management-sheet")))
(re-frame/reg-fx :hide-wallet-connect-app-management-sheet (fn [] (dissmiss-overlay "wallet-connect-app-management-sheet")))

;; SIGNING

(defonce signing-sheet-reg
  (.registerComponent Navigation
                      "signing-sheet"
                      (fn [] (gestureHandlerRootHOC views/signing-comp))
                      (fn [] views/signing-comp)))

(re-frame/reg-fx :show-signing-sheet (fn [] (show-overlay "signing-sheet")))
(re-frame/reg-fx :hide-signing-sheet (fn [] (dissmiss-overlay "signing-sheet")))

;; Select account
;; TODO why is this not a regular bottom sheet ?

(defonce select-acc-sheet-reg
  (.registerComponent Navigation
                      "select-acc-sheet"
                      (fn [] (gestureHandlerRootHOC views/select-acc-comp))
                      (fn [] views/select-acc-comp)))

(re-frame/reg-fx :show-select-acc-sheet (fn [] (show-overlay "select-acc-sheet")))
(re-frame/reg-fx :hide-select-acc-sheet (fn [] (dissmiss-overlay "select-acc-sheet")))

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
     (.pop Navigation (name @state/root-comp-id)))))

(re-frame/reg-fx
 :navigate-replace-fx
 (fn [view-id]
   (log/debug :navigate-replace-fx view-id)
   (.pop Navigation (name @state/root-comp-id))
   (navigate view-id)))

(def community-screens '(:community-management
                         :community-members
                         :community-requests-to-join
                         :create-community-channel
                         :community-emoji-thumbnail-picker
                         :create-community-category
                         :community-edit-chats
                         :community-edit
                         :community-reorder-categories))

;; change view-id if it is still same after component is disappeared
;; https://github.com/wix/react-native-navigation/issues/5744#issuecomment-563226820
(fx/defn view-disappeared
  {:events [::view-disappeared]}
  [{:keys [db]} view-id]
  (when (= view-id (:view-id db))
    {:db (assoc db :view-id (cond
                              (= view-id :community-emoji-thumbnail-picker)
                              :create-community-channel

                              (some #(= view-id %) community-screens)
                              :community

                              :else
                              :home))}))

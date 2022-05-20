(ns status-im.navigation2.core
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.views :as views]
            [status-im.navigation2.utils :as nav2-utils]
            [status-im.navigation2.roots :as roots]
            [status-im.navigation.roots :as nav-roots]
            ["react-native-navigation" :refer (Navigation)]))

(def tab-key-idx {:home        0
                  :communities 1
                  :wallet      2
                  :browser     3})

;; (defonce set-navigation-default-options
;;   (.setDefaultOptions Navigation (clj->js {:options {:topBar {:visible false}}})))

;; TODO (parvesh) - improve open-modal and close-modal
(defn open-modal [comp]
  (.showModal Navigation
              (clj->js {:stack {:children
                                [{:component
                                  {:name      comp
                                   :id        comp
                                   :options   {:topBar {:visible false}}}}]}})))

(defn close-modal [_])

(defn close-all-modals []
  (.dismissAllModals Navigation))

(defn get-id [comp id]
  (str comp "-" id))

(defn get-options [show-topbar? options]
  (if show-topbar?
    (merge options
           (nav-roots/status-bar-options)
           (nav-roots/merge-top-bar (nav-roots/topbar-options) options))
    {:topBar {:visible false}}))

(defn change-stack-root [[comp _]]
  (let [{:keys [options]} (get views/screens comp)]
    (.setStackRoot Navigation
                   (name comp)
                   (clj->js {:stack {:id       comp
                                     :children [{:component {:id      comp
                                                             :name    comp
                                                             :options (get-options false options)}}]}}))))

(defn navigate [[comp _]]
  (let [{:keys [options]} (get views/screens comp)]
    (reset! nav2-utils/container-stack-view-id comp)
    (.push Navigation
           (name :home-stack)
           (clj->js {:stack {:id       comp
                             :children [{:component {:id      comp
                                                     :name    comp
                                                     :options (get-options false options)}}]}}))))

(defn navigate-from-switcher [[comp id from-home?]]
  (if from-home?
    (navigate [comp id])
    (change-stack-root [comp id])))

(re-frame/reg-fx
 :init-root-fx-nav2
 (fn [new-root-id]
   (reset! nav2-utils/container-stack-view-id new-root-id)
   (.setRoot Navigation (clj->js (get (roots/roots) new-root-id)))))

(re-frame/reg-fx :open-modal-fx-nav2 open-modal)

(re-frame/reg-fx :close-modal-fx-nav2 close-modal)

(re-frame/reg-fx :navigate-to-fx-nav2 navigate)

(re-frame/reg-fx :navigate-from-switcher-fx navigate-from-switcher)

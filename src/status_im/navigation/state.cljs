(ns status-im.navigation.state
  (:require [utils.re-frame :as rf]))

(defonce root-id (atom nil))
(defonce modals (atom []))
(defonce dissmissing (atom false))
(defonce alert-banner-shown? (atom false))

(defonce ^:private navigation-state (atom []))

(defn get-navigation-state
  []
  @navigation-state)

(defn- update-view-id
  []
  (when-let [view-id (:id (last (get-navigation-state)))]
    (rf/dispatch [:set-view-id view-id])))

(defn navigation-state-push
  [component]
  (when-let [view-id (:id (last (get-navigation-state)))]
    (when-not (= view-id (:id component))
      (swap! navigation-state conj component)
      (update-view-id))))

(defn navigation-state-pop
  []
  (reset! navigation-state (vec (butlast @navigation-state)))
  (update-view-id))

(defn navigation-state-reset
  [state]
  (reset! navigation-state state)
  (update-view-id))

(defn- indices-of-predicate-match
  [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn navigation-pop-from
  "Pops all items after match (including match)"
  [comp-id]
  (when-let [index (first (indices-of-predicate-match #(= comp-id (:id %)) @navigation-state))]
    (navigation-state-reset (vec (take index @navigation-state)))))

(defn navigation-pop-after
  "Pops all items after match (excluding match)"
  [comp-id]
  (when-let [index (first (indices-of-predicate-match #(= comp-id (:id %)) @navigation-state))]
    (navigation-state-reset (vec (take (inc index) @navigation-state)))))

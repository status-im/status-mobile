(ns status-im.navigation.state)

(defonce root-id (atom nil))
(defonce pushed-screen-id (atom nil))
(defonce modals (atom []))
(defonce dissmissing (atom false))
(defonce navigation-state (atom []))

(defn navigation-state-push
  [component]
  (swap! navigation-state conj component))

(defn navigation-state-pop
  []
  (reset! navigation-state (vec (butlast @navigation-state))))

(defn navigation-state-reset
  [state]
  (reset! navigation-state state))

(defn indices
  [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn navigation-pop-from
  "Pops all items after match (including match)"
  [comp-id]
  (let [index (first (indices #(= comp-id (:id %)) @navigation-state))]
    (when index
      (navigation-state-reset (vec (take index @navigation-state))))))

(defn navigation-pop-after
  "Pops all items after match (excluding match)"
  [comp-id]
  (let [index (first (indices #(= comp-id (:id %)) @navigation-state))]
    (when index
      (navigation-state-reset (vec (take (inc index) @navigation-state))))))

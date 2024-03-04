(ns status-im.navigation.utils)

(defn add-view-to-modal-stack
  [modal-view-ids new-id]
  (if-let [current-stack (and (seq modal-view-ids) (last modal-view-ids))]
    (let [updated-stack (conj current-stack new-id)
          without-last  (vec (butlast modal-view-ids))]
      (conj without-last updated-stack))
    modal-view-ids))

(defn remove-last-view-from-current-modal-stack
  [modal-view-ids]
  (if (empty? modal-view-ids)
    modal-view-ids
    (let [last-stack         (last modal-view-ids)
          updated-last-stack (vec (butlast last-stack))
          without-last-stack (vec (butlast modal-view-ids))]
      (if (empty? updated-last-stack)
        without-last-stack
        (conj without-last-stack updated-last-stack)))))

(defn add-stack-to-modal-stacks
  [modal-view-ids first-view-id]
  (if (seq modal-view-ids)
    (conj modal-view-ids [first-view-id])
    [[first-view-id]]))

(defn remove-current-modal-stack
  [modal-view-ids]
  (if (seq modal-view-ids)
    (vec (butlast modal-view-ids))
    []))

(defn remove-views-from-modal-stack-until-comp-id
  [modal-view-ids comp-id]
  (let [found-index (first (keep-indexed (fn [idx stack]
                                           (when (some #{comp-id} stack) idx))
                                         modal-view-ids))]
    (if found-index
      (let [target-stack  (nth modal-view-ids found-index)
            comp-id-index (.indexOf target-stack comp-id)
            updated-stack (take (inc comp-id-index) target-stack)]
        (assoc modal-view-ids found-index updated-stack))
      modal-view-ids)))

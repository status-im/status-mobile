(ns syng-im.participants.handlers
  (:require [syng-im.navigation.handlers :as nav]
            [re-frame.core :refer [register-handler debug]]))

(defmethod nav/preload-data! :add-participants
  [db _]
  (assoc db :new-participants #{}))

(defmethod nav/preload-data! :remove-participants
  [db _]
  (assoc db :new-participants #{}))

(defn deselect-participant
  [db [_ id]]
  (update db :new-participants disj id))

(register-handler :deselect-participant deselect-participant)

(defn select-participant
  [db [_ id]]
  (update db :new-participants conj id))

(register-handler :select-participant (debug select-participant))

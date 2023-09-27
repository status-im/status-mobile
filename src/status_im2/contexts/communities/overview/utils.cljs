(ns status-im2.contexts.communities.overview.utils
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]))

(defn join-existing-users-string
  [user-list]
  (let [users-count (count user-list)
        first-two   (->> user-list
                         (take 2)
                         (map #(string/split (:full-name %) #" "))
                         (map first))]
    (case users-count
      0 ""
      1 (i18n/label :t/join-one-user {:user (first first-two)})
      2 (i18n/label :join-two-users
                    {:user1 (first first-two)
                     :user2 (second first-two)})
      (i18n/label :join-more-users
                  {:user1      (first first-two)
                   :user2      (second first-two)
                   :left-count (- users-count 2)}))))

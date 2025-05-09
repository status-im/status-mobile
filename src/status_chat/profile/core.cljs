(ns status-chat.profile.core)

(defn recently-opened
  [profiles]
  (first (sort-by :timestamp > (vals profiles))))

(defn key-uid
  [profile]
  (:key-uid profile))

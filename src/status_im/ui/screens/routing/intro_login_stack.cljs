(ns status-im.ui.screens.routing.intro-login-stack)

(def all-screens #{:login
                   :progress
                   :create-multiaccount
                   :recover
                   :multiaccounts
                   :intro
                   :intro-wizard})

(defn login-stack [view-id]
  {:name    :login-stack
   :screens [:login
             :progress
             :create-multiaccount
             :recover
             :multiaccounts]
   :config  (if (#{:login :progress :multiaccounts :enter-pin-login} view-id)
              {:initialRouteName view-id}
              {:initialRouteName :login})})

(defn intro-stack []
  (-> (login-stack :intro)
      (update :screens conj :intro :intro-wizard)
      (assoc :name :intro-stack)
      (assoc :config {:initialRouteName :intro})))

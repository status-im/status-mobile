(ns status-im.ui.screens.routing.screens
  (:require [status-im.ui.screens.multiaccounts.login.views :as login]
            [status-im.ui.screens.multiaccounts.recover.views :as recover]
            [status-im.ui.screens.multiaccounts.views :as multiaccounts]
            [status-im.ui.screens.intro.views :as intro]))

(def all-screens
  {:login                                            login/login
   :recover                                          recover/recover
   :multiaccounts                                    multiaccounts/multiaccounts
   :intro                                            intro/intro})

(defn get-screen [screen]
  (get all-screens screen #(throw (str "Screen " screen " is not defined."))))

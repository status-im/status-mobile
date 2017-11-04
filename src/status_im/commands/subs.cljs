(ns status-im.commands.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-commands-responses-by-access-scope
  (fn [db _]
    (:access-scope->commands-responses db)))

(reg-sub
  :get-command
  :<- [:get-contacts]
  (fn [contacts [_ ref]] 
    (some->> ref (get-in contacts))))

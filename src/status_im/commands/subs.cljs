(ns status-im.commands.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :get-commands-responses-by-access-scope :access-scope->commands-responses)

(reg-sub
 :get-command
 :<- [:get-contacts]
 (fn [contacts [_ ref]]
   (some->> ref (get-in contacts))))

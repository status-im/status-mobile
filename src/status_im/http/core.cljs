(ns status-im.http.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.http :as http]))

(re-frame/reg-fx
 :http-post
 (fn [{:keys [url data response-validator on-success on-error timeout-ms opts]}]
   (let [all-opts (assoc opts
                         :valid-response? response-validator
                         :timeout-ms      timeout-ms)]
     (http/post url data on-success on-error all-opts))))

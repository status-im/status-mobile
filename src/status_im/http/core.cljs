(ns status-im.http.core
  (:require [status-im.utils.http :as http]
            [re-frame.core :as re-frame]))

(re-frame/reg-fx
 :http-get
 (fn [{:keys [url response-validator on-success on-error timeout-ms]}]
   (let [opts {:valid-response? response-validator
               :timeout-ms      timeout-ms}]
     (http/get url on-success on-error opts))))

(re-frame/reg-fx
 :http-get-n
 (fn [calls]
   (doseq [{:keys [url response-validator on-success on-error timeout-ms]} calls]
     (let [opts {:valid-response? response-validator
                 :timeout-ms      timeout-ms}]
       (http/get url on-success on-error opts)))))

(re-frame/reg-fx
 :http-post
 (fn [{:keys [url data response-validator on-success on-error timeout-ms opts]}]
   (let [all-opts (assoc opts
                         :valid-response? response-validator
                         :timeout-ms timeout-ms)]
     (http/post url data on-success on-error all-opts))))

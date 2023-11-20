#!/usr/bin/env node

import https from 'node:https'
import { basename } from 'node:path'
import { promisify } from 'node:util'
import { createReadStream } from 'node:fs'

import log from 'npmlog'
import FormData from 'form-data'

const UPLOAD_URL = 'https://upload.diawi.com/'
const STATUS_URL = 'https://upload.diawi.com/status'
const POLL_MAX_COUNT = 10
const POLL_INTERVAL_MS = 700

const sleep = (ms) => {
  return new Promise((resolve, reject) => {
    setTimeout(resolve, (ms))
  })
}

const getRequest = async (url) => {
  return new Promise((resolve, reject) => {
    let data = []
    https.get(url, res => {
      res.on('error', err => reject(err))
      res.on('data', chunk => { data.push(chunk) })
      res.on('end', () => {
        let payload = Buffer.concat(data).toString()
        resolve({
          code: res.statusCode,
          message: res.statusMessage,
          payload: payload,
        })
      })
    })
  })
}

const uploadIpa = async (ipaPath, comment, token) => {
  let form = new FormData()
  form.append('token', token)
  form.append('file', createReadStream(ipaPath))
  form.append('comment', comment || basename(ipaPath))

  const formSubmitPromise = promisify(form.submit.bind(form))

  const res = await formSubmitPromise(UPLOAD_URL)
  if (res.statusCode != 200) {
    log.error('uploadIpa', 'Upload failed: %d %s', res.statusCode, res.statusMessage)
    process.exit(1)
  }

  return new Promise((resolve) => {
    const jobId = res.on('data', async (data) => {
      resolve(JSON.parse(data)['job'])
    })
  })
}

const checkStatus = async (jobId, token) => {
  let params = new URLSearchParams({
    token: token, job: jobId,
  })
  let rval = await getRequest(`${STATUS_URL}?${params.toString()}`)
  if (rval.code != 200) {
    log.error('checkStatus', 'Check query failed: %d %s', rval.code, rval.message)
    process.exit(1)
  }
  return JSON.parse(rval.payload)
}

const pollStatus = async (jobId, token) => {
  let interval = POLL_INTERVAL_MS
  for (let i = 0; i <= POLL_MAX_COUNT; i++) {
    let json = await checkStatus(jobId, token)
    switch (json.status) {
      case 2000:
        return json
      case 2001:
        log.verbose('pollStatus', 'Waiting: %s', json.message)
        break /* Nothing, just poll again. */
      case 4000000:
        log.warning('pollStatus', 'Doubling polling interval: %s', json.message)
        interval *= 2
        break
      default:
        log.error('pollStatus', `Error in status response: ${json.message}`)
        process.exit(1)
    }
    await sleep(interval)
  }
}

const main = async () => {
  const token = process.env.DIAWI_TOKEN
  const targetFile = process.argv[2]
  const comment = process.argv[3]
  log.level = process.env.VERBOSE ? 'verbose' : 'info'

  if (token === undefined) {
    log.error('main', 'No DIAWI_TOKEN env var provided!')
    process.exit(1)
  }
  if (targetFile === undefined) {
    log.error('main', 'No file path provided!')
    process.exit(1)
  }

  log.info('main', 'Uploading: %s', targetFile)
  let jobId = await uploadIpa(targetFile, comment, token)

  log.info('main', 'Polling upload job status: %s', jobId)
  let uploadMeta = await pollStatus(jobId, token)

  console.log(uploadMeta)
}

main()

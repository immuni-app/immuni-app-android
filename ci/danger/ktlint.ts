// ktlint.ts
// Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
// Please refer to the AUTHORS file for more information.
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

import { fail, message } from "./danger";
import { promisify } from "util";
import { relative } from "path";

const exec = promisify(require("child_process").exec);

// taken from https://github.com/pinterest/ktlint/blob/master/ktlint-reporter-json/src/main/kotlin/com/pinterest/ktlint/reporter/json/JsonReporter.kt
type ktlintError = {
  line: number;
  column: number;
  message: string;
  rule: string;
};
type ktlintReport = {
  file: string;
  errors: ktlintError[];
};

export default async (path: string) => {
  let reports: ktlintReport[];
  let toolVersion: string;

  try {
    toolVersion = (await exec("ktlint --version")).stdout
      .replace(/\s+/g, " ")
      .trim();

    const { stdout } = await exec(`ktlint --reporter=json || true`, {
      cwd: path,
      encoding: "utf8"
    });

    reports = JSON.parse(stdout) as ktlintReport[];
    ktlintReportToDanger(path, reports, toolVersion);
  } catch (error) {
    // if there are errors, the exit code is not 0 and the exec fn throws
    const { killed, code } = error;

    if (killed || code != 2) {
      fail(
        `ktlint cannot be executed. This is a CI error killed: ${killed}, code: ${code}`
      );
    } else {
      reports = JSON.parse(error.stdout) as ktlintReport[];
      ktlintReportToDanger(path, reports, toolVersion);
    }
  }
};

const ktlintReportToDanger = (
  path: string,
  reports: ktlintReport[],
  toolVersion: string
) => {
  if (reports.length == 0) {
    message(`:white_check_mark: ktlint passed`);
    return;
  }

  // ktlint only supports errors, not warnings
  for (const report of reports) {
    report.errors.forEach(function(error) {
      const reportMessage = `ktlint (${toolVersion})<br>parent path: ${path}<br><br>Issue: ${
        error.message
      } (rule: ${error.rule})`;
      const file = relative(path, report.file);
      fail(reportMessage, file, error.line);
    });
  }
};

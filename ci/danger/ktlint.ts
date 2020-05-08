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
}
type ktlintReport = {
  file: string;
  errors: ktlintError[];
};

export default async (path: string) => {
  let reports: ktlintReport[];
  let toolVersion: string;

  try {
    toolVersion = (
      await exec("ktlint --version")
    ).stdout.replace(/\s+/g, ' ').trim();

    const { stdout } = await exec(
      `ktlint --reporter=json || true`,
      { cwd: path, encoding: "utf8" }
    );

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

const ktlintReportToDanger = (path: string, reports: ktlintReport[], toolVersion: string) => {
  if (reports.length == 0) {
    message(`:white_check_mark: ktlint passed`);
    return;
  }

  // ktlint only supports errors, not warnings
  for (const report of reports) {
    report.errors.forEach(function (error) {
      const reportMessage = `ktlint (${toolVersion})<br>parent path: ${path}<br><br>Issue: ${error.message} (rule: ${error.rule})`;
      const file = relative(path, report.file);
      fail(reportMessage, file, error.line);
    });
  }
};

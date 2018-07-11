using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Serie3
{
    public class ResultParallel
    {
        public List<string> list;
        public int numberOfFiles;
        public ResultParallel(int filesToProcess)
        {
            this.list = new List<string>(filesToProcess);
        }
    }


    public class Ex2
    {

        public static ResultParallel processDir(string dirPath, int numberOfFiles, CancellationToken ctoken, Action<Object, Object> progress)
        {
            ResultParallel result = new ResultParallel(numberOfFiles);
            string[] arr = Directory.GetFiles(dirPath);
            ParallelOptions ops = new ParallelOptions{
                CancellationToken = ctoken,
                MaxDegreeOfParallelism = Environment.ProcessorCount
            };
            Dictionary<string, long> map = new Dictionary<string, long>();
            object _lock = new object();
            int countFiles = 0;
            Parallel.ForEach(arr, ops, () => new List<FileInfo>(),
                (item, loopstate, partial) =>
                {
                    try
                    {
                        ctoken.ThrowIfCancellationRequested();
                    }
                    catch { }  
                    if (loopstate.ShouldExitCurrentIteration)
                    {
                        //... optonally exit this iteration early
                        return partial;
                    }
                    partial.Add(new FileInfo(item));
                    return partial;  
                },
                (partialList) =>
                {
                    lock (_lock)
                    {
                        foreach (FileInfo partial in partialList)
                        {
                            if (!partial.Exists) continue;
                            countFiles++;
                            progress.Invoke(countFiles,arr.Length);
                            map.Add(partial.FullName, partial.Length);
                            if (result.list.Count < result.list.Capacity)
                            {
                                result.list.Add(partial.FullName);
                                continue;
                            }
                            long min = Int64.MaxValue;
                            string file_sub = null;
                            foreach (string s in result.list)
                            {
                                if (map[s] < min)
                                {
                                    min = map[s];
                                    file_sub = s;
                                }
                            }
                            if (min < partial.Length)
                            {
                                result.list.Remove(file_sub);
                                result.list.Add(partial.FullName);
                            }
                        }
                    }
                });
            result.numberOfFiles = countFiles;
            return result;
        }

        public static Task<ResultParallel> processDirAsync(string dirPath, int numberOfFiles, CancellationToken ctoken, Action<Object,Object> progress)
        {
            return Task.Factory.StartNew(() =>
            {
                return processDir(dirPath, numberOfFiles, ctoken, progress);

            }, ctoken, TaskCreationOptions.LongRunning, TaskScheduler.Default);
        }
    }
    public class Test
    {

        public static void Main(string[] args) { 
            CancellationTokenSource cts = new CancellationTokenSource();
            CancellationToken ct = cts.Token;

            ResultParallel p = Ex2.processDir("D:\\2ºsemestre\\dumpFiles", 3, ct, (value1, value2) => { } );
            Console.WriteLine("Number of items processed {0}", p.numberOfFiles);
            p.list.ForEach((item) =>
            {
                Console.WriteLine(item);
            });
            Console.ReadLine();
        }

    }
}
